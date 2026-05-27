package br.gov.sifap.programas.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IndiceIpcaRepository extends JpaRepository<IndiceIpcaJpaEntity, IndiceIpcaJpaEntity.PK> {
    @Query("select i from IndiceIpcaJpaEntity i where (i.ano > :anoIni or (i.ano = :anoIni and i.mes >= :mesIni)) "
         + "and (i.ano < :anoFim or (i.ano = :anoFim and i.mes <= :mesFim)) order by i.ano, i.mes")
    List<IndiceIpcaJpaEntity> intervalo(int anoIni, int mesIni, int anoFim, int mesFim);
}
