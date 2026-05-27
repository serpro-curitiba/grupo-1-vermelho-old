# 🏗️ Mapeamento Técnico: CADBENEF → Spring Boot

**Preparado por:** Agente Archaeologist  
**Data:** 2026-05-27  
**Tipo:** Technical Specification para Arquiteto de Software

---

## 📌 Quick Facts

| Aspecto | Detalhe |
|---------|---------|
| **Linhas de código** | 318 linhas (incluindo subroutina) |
| **Complexidade ciclomática** | 7 (média) |
| **Número de validações** | 13 regras de negócio (BR-001 a BR-013) |
| **Anomalias identificadas** | 12 mistérios (M-001 a M-012) |
| **Impacto em data** | Arquivo 150 (BENEFICIARIO) |
| **Operações CRUD** | C (INSERT), R (SELECT), U (UPDATE) |
| **Transações** | 2 (uma para INSERT, uma para UPDATE) |
| **Dependências externas** | 0 (nenhuma CALLNAT) |

---

## 🎯 Bounded Context: Beneficiary Management

### Aggregates

```yaml
# AGGREGATE: Beneficiary (Raiz de Agregação)
Beneficiary:
  identity: CPF (N11)
  attributes:
    - name: String (60)
    - dateOfBirth: LocalDate
    - gender: Gender enum (M|F)
    - status: BeneficiaryStatus enum (A=ACTIVE|S=SUSPENDED)
    - socialSecurityNumber: Long (NIS)
    - familyIncome: BigDecimal (9,2)
    - dependentsCount: Integer (0-99)
    - registrationDate: LocalDate
    - lastUpdateDate: LocalDate
    
  valueObjects:
    - address: Address
      - street: String (80)
      - city: String (40)
      - state: String (2)
      - zipCode: String (8)
    - contact: Contact
      - phone: String (15)
      - identificationDocument: String (15) [RG]
    - programEnrollment: ProgramEnrollment
      - programCode: Integer (4)
      - regionCode: Integer (2)
    - validation: CpfValidation
      - algorithm: MOD11_RECEITA_FEDERAL
      - isValid: Boolean
      - verificationDigit1: Integer
      - verificationDigit2: Integer

### Repositories

```java
interface BeneficiaryRepository {
    BeneficiaryEntity findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    BeneficiaryEntity save(BeneficiaryEntity entity);
    BeneficiaryEntity update(String cpf, BeneficiaryEntity entity);
    void delete(String cpf);
}
```

### Domain Services

```java
// Validação de CPF (serviço de domínio)
@DomainService
class CpfValidationService {
    public CpfValidationResult validate(String cpf) {
        // Implementar algoritmo MOD-11 (linhas 267-318)
        // Retornar resultado com isValid() e detalhes
    }
}

// Lógica de negócio de idade
@DomainService
class AgeCalculationService {
    public Integer calculateAge(LocalDate dateOfBirth) {
        // CORREÇÃO: usar ano+mês+dia, não apenas ano (veja M-002)
    }
    
    public BeneficiaryStatus determineStatusByAge(Integer age) {
        // BR-011: se age > 75 → SUSPENDED
        return age > 75 ? BeneficiaryStatus.SUSPENDED : BeneficiaryStatus.ACTIVE;
    }
}

// Validação de referencias externas
@DomainService
class ExternalReferenceValidationService {
    public void validateProgramCode(Integer code) {
        // BR-???: COD-PROGRAMA deve existir (NOVO)
        // FIND PROGRAMA WITH COD-PROGRAMA = code
    }
    
    public void validateRegionCode(Integer code) {
        // BR-???: COD-REGIAO deve existir (NOVO)
        // FIND REGIAO WITH COD-REGIAO = code
    }
    
    public void validateNis(Long nis) {
        // BR-???: NIS validation com INSS (NOVO)
    }
}
```

### Use Cases

```java
@UseCase
class CreateBeneficiaryUseCase {
    // Corresponde a OPERACAO = 'I' (linhas 196-210)
    
    @input  BeneficiaryDTO dto
    @output BeneficiaryEntity created
    @errors BeneficiaryAlreadyExistsException,
            InvalidCpfException,
            InvalidAgeException
    
    @Transactional
    public BeneficiaryEntity execute(BeneficiaryDTO dto) {
        // 1. Validar CPF (BR-002, BR-003)
        cpfValidation.validate(dto.getCpf());
        
        // 2. Validar nome (BR-004)
        // 3. Validar data nascimento (BR-005)
        // 4. Validar sexo (BR-006)
        // 5. Verificar dupla (BR-007)
        beneficiaryRepository.findByCpf(dto.getCpf())
            .ifPresent(b -> throw new BeneficiaryAlreadyExistsException());
        
        // 6. Calcular idade (BR-009)
        Integer age = ageCalculationService.calculateAge(dto.getDateOfBirth());
        
        // 7. Determinar status (BR-010, BR-011)
        BeneficiaryStatus status = ageCalculationService.determineStatusByAge(age);
        
        // 8. Validar referencias (NOVO)
        externalRefValidation.validateProgramCode(dto.getProgramCode());
        externalRefValidation.validateRegionCode(dto.getRegionCode());
        externalRefValidation.validateNis(dto.getNis());
        
        // 9. Gravar
        BeneficiaryEntity entity = new BeneficiaryEntity(
            cpf: dto.getCpf(),
            name: dto.getName(),
            dateOfBirth: dto.getDateOfBirth(),
            gender: dto.getGender(),
            status: status,
            registrationDate: LocalDate.now(),
            lastUpdateDate: LocalDate.now(),
            ...
        );
        
        return beneficiaryRepository.save(entity);
    }
}

@UseCase
class UpdateBeneficiaryUseCase {
    // Corresponde a OPERACAO = 'A' (linhas 212-225)
    
    @input  String cpf, BeneficiaryUpdateDTO dto
    @output BeneficiaryEntity updated
    @errors BeneficiaryNotFoundException
    
    @Transactional
    public BeneficiaryEntity execute(String cpf, BeneficiaryUpdateDTO dto) {
        // 1. Verificar existência (BR-008)
        BeneficiaryEntity existing = beneficiaryRepository.findByCpf(cpf)
            .orElseThrow(BeneficiaryNotFoundException::new);
        
        // 2. Validar campos atualizáveis (BR-013)
        // CPF, dateOfBirth, gender, registrationDate são IMUTÁVEIS
        
        // 3. Atualizar campos permitidos
        existing.setName(dto.getName());
        existing.setAddress(dto.getAddress());
        existing.setPhone(dto.getPhone());
        existing.setStatus(dto.getStatus());
        existing.setFamilyIncome(dto.getFamilyIncome());
        existing.setDependentsCount(dto.getDependentsCount());
        existing.setLastUpdateDate(LocalDate.now());
        
        // 4. Gravar
        return beneficiaryRepository.update(cpf, existing);
    }
}
```

---

## 🔄 API REST Mapping

### Endpoint 1: Create Beneficiary

```http
POST /api/v1/beneficiaries
Content-Type: application/json

{
  "cpf": "11144477735",
  "name": "João da Silva",
  "dateOfBirth": "1980-05-15",
  "gender": "M",
  "address": {
    "street": "Rua das Flores, 123",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01234567"
  },
  "contact": {
    "phone": "(11) 99999-9999",
    "identificationDocument": "1234567890-X"
  },
  "programCode": 2024,
  "regionCode": 1,
  "familyIncome": 1500.50,
  "dependentsCount": 2,
  "socialSecurityNumber": 12345678901
}
```

**Response 201 Created:**
```json
{
  "cpf": "11144477735",
  "name": "João da Silva",
  "status": "A",
  "registrationDate": "2026-05-27",
  "lastUpdateDate": "2026-05-27"
}
```

**Error Responses:**
- `400 Bad Request`: Campos inválidos ou CPF já existe (BR-007)
- `422 Unprocessable Entity`: CPF inválido (BR-003), sexo inválido (BR-006)

---

### Endpoint 2: Update Beneficiary

```http
PUT /api/v1/beneficiaries/11144477735
Content-Type: application/json

{
  "name": "João da Silva Updated",
  "address": { ... },
  "status": "S",
  ...
}
```

**Response 200 OK:**
```json
{
  "cpf": "11144477735",
  "name": "João da Silva Updated",
  "dateOfBirth": "1980-05-15",  ← IMUTÁVEL
  "gender": "M",                ← IMUTÁVEL
  "status": "S",
  "registrationDate": "2026-05-27",  ← IMUTÁVEL
  "lastUpdateDate": "2026-05-27"
}
```

**Error Responses:**
- `404 Not Found`: CPF não existe (BR-008)
- `400 Bad Request`: Tentativa de alterar campo imutável (BR-013)

---

## 📊 Entity Mapping (JPA)

### BENEFICIARIO → BeneficiaryEntity

```java
@Entity
@Table(name = "BENEFICIARIO", indexes = {
    @Index(name = "idx_cpf", columnList = "cpf"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_region", columnList = "cod_regiao")
})
public class BeneficiaryEntity {
    
    @Id
    @Column(name = "cpf", length = 11, nullable = false)
    private String cpf;
    
    @Column(name = "nome", length = 60, nullable = false)
    private String name;
    
    @Column(name = "dt_nascimento", nullable = false)
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Gender gender;  // M | F
    
    @Column(name = "endereco", length = 80)
    private String street;
    
    @Column(name = "municipio", length = 40)
    private String city;
    
    @Column(name = "uf", length = 2)
    private String state;
    
    @Column(name = "cep", length = 8)
    private String zipCode;
    
    @Column(name = "telefone", length = 15)
    private String phone;
    
    @Column(name = "rg", length = 15)
    private String identificationDocument;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BeneficiaryStatus status;  // A | S
    
    @Column(name = "cod_programa", nullable = false)
    private Integer programCode;
    
    @Column(name = "renda_familiar", precision = 11, scale = 2)
    private BigDecimal familyIncome;
    
    @Column(name = "num_dependentes")
    private Integer dependentsCount;
    
    @Column(name = "dt_cadastro", nullable = false)
    private LocalDate registrationDate;
    
    @Column(name = "dt_atualizacao", nullable = false)
    private LocalDate lastUpdateDate;
    
    @Column(name = "cod_regiao", nullable = false)
    private Integer regionCode;
    
    @Column(name = "nis", length = 11)
    private Long socialSecurityNumber;
    
    // ... getters, setters, constructors
}

// Enums
public enum Gender {
    M("Masculino"),
    F("Feminino");
}

public enum BeneficiaryStatus {
    A("Ativo"),
    S("Suspenso");
}
```

---

## ⚡ Validations (Spring Validation)

```java
public class BeneficiaryDTO {
    
    @NotNull
    @Size(min = 11, max = 11)
    @Pattern(regexp = "\\d{11}")
    private String cpf;  // BR-002
    
    @NotBlank
    @Size(max = 60)
    private String name;  // BR-004
    
    @NotNull
    @PastOrPresent
    private LocalDate dateOfBirth;  // BR-005, + validação de data real
    
    @NotNull
    @Pattern(regexp = "[MF]")
    private Gender gender;  // BR-006
    
    @NotNull
    @Min(1)
    @Max(9999)
    private Integer programCode;  // BR-???: validar existência (NOVO)
    
    @NotNull
    @Min(1)
    @Max(99)
    private Integer regionCode;  // BR-???: validar existência (NOVO)
    
    @DecimalMin("0.00")
    @DecimalMax("9999999.99")
    private BigDecimal familyIncome;  // M-008: validações adicionais
    
    @Min(0)
    @Max(99)
    private Integer dependentsCount;  // M-008: validações adicionais
    
    @Size(min = 11, max = 11)
    private Long socialSecurityNumber;  // M-007: adicionar validação NIS
    
    // Custom validators via @Validated
    @CpfValidator  // Validar módulo 11
    private String cpf;
    
    @UniqueEntityValidator
    private String cpf;  // BR-007: em create
    
    @ProgramCodeExistsValidator
    private Integer programCode;  // BR-???: validar em tabela PROGRAMA
    
    @RegionCodeExistsValidator
    private Integer regionCode;  // BR-???: validar em tabela REGIAO
}
```

### Custom Validators

```java
@Target({ PARAMETER, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = CpfValidator.class)
public @interface ValidCpf {
    String message() default "Invalid CPF (checksum error)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Component
public class CpfValidator implements ConstraintValidator<ValidCpf, String> {
    
    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null) return true;  // @NotNull handles this
        
        // Implementar BR-003 (linhas 267-318)
        return validateCpfMod11(cpf);
    }
    
    private boolean validateCpfMod11(String cpf) {
        // Digit extraction, mod 11 calculation, validation
        // Espelhar exatamente a lógica de VALIDA-CPF
    }
}
```

---

## 🗄️ Database Schema

```sql
-- DDL Equivalente (PostgreSQL 16)

CREATE TABLE BENEFICIARIO (
    cpf VARCHAR(11) PRIMARY KEY NOT NULL,
    nome VARCHAR(60) NOT NULL,
    dt_nascimento DATE NOT NULL,
    sexo CHAR(1) NOT NULL CHECK (sexo IN ('M', 'F')),
    endereco VARCHAR(80),
    municipio VARCHAR(40),
    uf CHAR(2),
    cep VARCHAR(8),
    telefone VARCHAR(15),
    rg VARCHAR(15),
    status CHAR(1) NOT NULL DEFAULT 'A' CHECK (status IN ('A', 'S')),
    cod_programa NUMERIC(4) NOT NULL,
    renda_familiar NUMERIC(9,2),
    num_dependentes NUMERIC(2),
    dt_cadastro DATE NOT NULL DEFAULT CURRENT_DATE,
    dt_atualizacao DATE NOT NULL DEFAULT CURRENT_DATE,
    cod_regiao NUMERIC(2) NOT NULL,
    nis NUMERIC(11),
    
    -- Foreign Keys (NOVO)
    CONSTRAINT fk_programa FOREIGN KEY (cod_programa) REFERENCES PROGRAMA(cod_programa),
    CONSTRAINT fk_regiao FOREIGN KEY (cod_regiao) REFERENCES REGIAO(cod_regiao),
    
    -- Triggers (NOVO)
    TRIGGER trg_beneficiario_update BEFORE UPDATE ON BENEFICIARIO
        FOR EACH ROW SET dt_atualizacao = CURRENT_DATE
);

-- Índices para performance
CREATE INDEX idx_beneficiario_status ON BENEFICIARIO(status);
CREATE INDEX idx_beneficiario_regiao ON BENEFICIARIO(cod_regiao);
CREATE INDEX idx_beneficiario_programa ON BENEFICIARIO(cod_programa);
```

---

## 🧪 Test Mapping

### Testes Unitários

```java
@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
public class CpfValidationServiceTest {
    
    @InjectMocks
    private CpfValidationService service;
    
    @Test
    public void testValidCpf() {
        // Caso de teste: BR-003
        String validCpf = "11144477735";
        CpfValidationResult result = service.validate(validCpf);
        assertTrue(result.isValid());
    }
    
    @Test
    public void testInvalidCpf_FirstDigitError() {
        // Teste com dígito verificador 1 errado
        String invalidCpf = "11144477734";  // DV1 errado
        CpfValidationResult result = service.validate(invalidCpf);
        assertFalse(result.isValid());
    }
    
    @Test
    public void testInvalidCpf_SecondDigitError() {
        // Teste com dígito verificador 2 errado
        String invalidCpf = "11144477636";  // DV2 errado
        CpfValidationResult result = service.validate(invalidCpf);
        assertFalse(result.isValid());
    }
}

@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
public class AgeCalculationServiceTest {
    
    @InjectMocks
    private AgeCalculationService service;
    
    @Test
    public void testAgeCalculation_ReturnsCorrectAge() {
        // Corrigir M-002: usar ano+mês+dia
        LocalDate birthDate = LocalDate.of(1980, 5, 15);
        LocalDate today = LocalDate.of(2026, 5, 27);
        
        // Mockando LocalDate.now()
        // age = 46 anos (correto)
        
        Integer age = service.calculateAge(birthDate);
        assertEquals(46, age);
    }
    
    @Test
    public void testStatusDetermination_AgeLessThan75() {
        // BR-010 e BR-011
        BeneficiaryStatus status = service.determineStatusByAge(50);
        assertEquals(BeneficiaryStatus.ACTIVE, status);
    }
    
    @Test
    public void testStatusDetermination_AgeGreaterThan75() {
        // BR-011
        BeneficiaryStatus status = service.determineStatusByAge(76);
        assertEquals(BeneficiaryStatus.SUSPENDED, status);
    }
    
    @Test
    public void testStatusDetermination_AgeExactly75() {
        // BR-011: boundary test
        BeneficiaryStatus status = service.determineStatusByAge(75);
        assertEquals(BeneficiaryStatus.ACTIVE, status);
    }
}
```

### Testes de Integração (TestContainers)

```java
@SpringBootTest
@Testcontainers
public class BeneficiaryRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16-alpine");
    
    @Autowired
    private BeneficiaryRepository repository;
    
    @Test
    public void testCreateAndFindBeneficiary() {
        // BR-001 a BR-012: Create
        BeneficiaryEntity entity = new BeneficiaryEntity(
            cpf = "11144477735",
            name = "Test User",
            dateOfBirth = LocalDate.of(1980, 5, 15),
            gender = Gender.M,
            status = BeneficiaryStatus.ACTIVE,
            ...
        );
        
        BeneficiaryEntity saved = repository.save(entity);
        
        BeneficiaryEntity found = repository.findByCpf("11144477735");
        assertNotNull(found);
        assertEquals("Test User", found.getName());
    }
    
    @Test
    public void testDuplicateCpf_ThrowsException() {
        // BR-007: Prevent duplicate CPF on INSERT
        
        BeneficiaryEntity entity1 = createBeneficiary("11144477735");
        repository.save(entity1);
        
        BeneficiaryEntity entity2 = createBeneficiary("11144477735");
        
        assertThrows(
            DataIntegrityViolationException.class,
            () -> repository.save(entity2)
        );
    }
    
    @Test
    public void testUpdateBeneficiary_ImmutableFields() {
        // BR-013: Immutable fields (CPF, dateOfBirth, gender)
        
        BeneficiaryEntity entity = repository.save(createBeneficiary("11144477735"));
        
        entity.setName("New Name");  ✓ OK
        entity.setCpf("99988877766");  ✗ Should not update
        entity.setDateOfBirth(LocalDate.of(1990, 1, 1));  ✗ Should not update
        entity.setGender(Gender.F);  ✗ Should not update
        
        BeneficiaryEntity updated = repository.update("11144477735", entity);
        
        assertEquals("11144477735", updated.getCpf());  // Unchanged
        assertEquals(LocalDate.of(1980, 5, 15), updated.getDateOfBirth());  // Unchanged
        assertEquals(Gender.M, updated.getGender());  // Unchanged
        assertEquals("New Name", updated.getName());  // Changed
    }
}
```

---

## ⚙️ Configuration (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sifap
    username: sifap_user
    password: ${SIFAP_DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
  
  jpa:
    hibernate:
      ddl-auto: validate  # Nenhuma alteração automática
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQL16Dialect
        jdbc:
          batch_size: 20
          fetch_size: 50
        format_sql: true
  
  validation:
    enabled: true

logging:
  level:
    org.hibernate: WARN
    org.hibernate.SQL: DEBUG
    br.gov.sifap.beneficiary: DEBUG
```

---

## 📋 Checklist de Implementação

- [ ] Criar BeneficiaryEntity (JPA)
- [ ] Criar BeneficiaryRepository
- [ ] Criar BeneficiaryDTO e BeneficiaryUpdateDTO
- [ ] Implementar CpfValidationService (MOD-11)
- [ ] Implementar AgeCalculationService
- [ ] Implementar ExternalReferenceValidationService (NOVO)
- [ ] Criar custom validators (CpfValidator, UniqueEntityValidator, etc.)
- [ ] Criar CreateBeneficiaryUseCase
- [ ] Criar UpdateBeneficiaryUseCase
- [ ] Criar BeneficiaryController com endpoints REST
- [ ] Criar unit tests (CpfValidationServiceTest, etc.)
- [ ] Criar integration tests (TestContainers)
- [ ] Criar API documentation (Swagger/OpenAPI)
- [ ] Implementar auditoria (criador, modificador, timestamps completos)
- [ ] Implementar soft delete (se necessário)
- [ ] Criar scripts de migração de dados (Flyway)
- [ ] Performance testing (índices, query optimization)
- [ ] Security review (SQL injection, mass assignment, etc.)

---

## 🚨 Críticas para Arquiteto

### Issues Críticas Identificadas

1. **M-002: Cálculo de idade impreciso**
   - Corrente: `age = year(now) - year(birthdate)` ❌
   - Esperado: Considerar mês e dia ✅
   - Impacto: BR-011 pode aplicar suspensão no dia errado

2. **M-003: Limite de 75 anos não documentado**
   - Questão aberta desde 2011
   - Recomendação: Entrevistar negócio ANTES de implementar

3. **M-006, M-007: Validações de referências ausentes**
   - COD-PROGRAMA, COD-REGIAO não validados
   - NIS sem validação
   - Recomendação: Adicionar FKs e validação

4. **M-010: Transações sem garantia ACID**
   - Adabas vs. PostgreSQL diferenças
   - Recomendação: Testar behavior com TestContainers

5. **M-011: Sem timestamp completo**
   - Apenas data, sem hora
   - Recomendação: Usar `OffsetDateTime` em Spring Boot

---

## 📚 Referências

- **Original Program:** `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN` (318 linhas)
- **Analysis Report:** `CADBENEF-ANALISE-ARQUEOLOGICA.yaml` (detalhado)
- **Executive Summary:** `CADBENEF-SUMARIO-EXECUTIVO.md` (visual)
- **Spring Boot Guide:** https://spring.io/projects/spring-boot
- **JPA Best Practices:** https://docs.oracle.com/cd/E19798-01/821-1841/bnbqc/index.html
- **CPF Validation Algorithm:** Receita Federal Brasil (módulo 11)

---

**Próximo Passo:** Passar para @architect para especificação EARS
