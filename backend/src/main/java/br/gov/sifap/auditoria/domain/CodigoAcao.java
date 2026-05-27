package br.gov.sifap.auditoria.domain;

/**
 * Códigos de ação do DDM AUDITORIA — paridade ARQ 153.
 * <pre>
 *  IN = Inclusão       AL = Alteração      EX = Exclusão       CO = Consulta
 *  CN = Cancelamento   LG = Login          LO = Logoff         BT = Batch
 *  ER = Erro           AU = Auditoria      RE = Reprocesso     DV = Devolução
 * </pre>
 */
public enum CodigoAcao {
    IN, AL, EX, CO, CN, LG, LO, BT, ER, AU, RE, DV
}
