package br.gov.sifap.relatorios.application;

import br.gov.sifap.auditoria.infrastructure.persistence.EventoJpaEntity;
import br.gov.sifap.auditoria.infrastructure.persistence.EventoRepository;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Substitui RELAUDIT.NSN — trilha de auditoria por período/usuário/ação.
 *
 * <p><b>Quirk legado mantido:</b> por padrão, ações de código 'EX' (exclusão)
 * são <em>ocultadas</em> do relatório — bug histórico do RELAUDIT, replicado
 * para manter os números idênticos. Para ver exclusões, passe {@code incluirExclusoes=true}.</p>
 */
@Service
public class TrilhaAuditoriaUseCase {

    private final EventoRepository repo;

    public TrilhaAuditoriaUseCase(EventoRepository repo) { this.repo = repo; }

    public List<EventoJpaEntity> executar(OffsetDateTime de, OffsetDateTime ate,
                                          String acao, String usuarioId, String agregado,
                                          boolean incluirExclusoes) {
        var lista = repo.filtrar(de, ate, acao, usuarioId, agregado);
        if (!incluirExclusoes) {
            return lista.stream().filter(e -> !"EX".equals(e.getAcao())).toList();
        }
        return lista;
    }
}
