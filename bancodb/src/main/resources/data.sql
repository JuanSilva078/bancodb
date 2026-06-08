-- Dados iniciais para ContaBancaria (sem versão)
INSERT INTO conta_bancaria (id, titular, saldo) VALUES (1, 'João Silva', 1000.00);
INSERT INTO conta_bancaria (id, titular, saldo) VALUES (2, 'Maria Souza', 500.00);

-- Dados iniciais para ContaBancariaVersionada (com @Version)
INSERT INTO conta_bancaria_versionada (id, titular, saldo, version) VALUES (1, 'João Silva (Versionado)', 1000.00, 0);
INSERT INTO conta_bancaria_versionada (id, titular, saldo, version) VALUES (2, 'Maria Souza (Versionado)', 500.00, 0);
