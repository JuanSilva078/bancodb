# BancoDB — Controle de Concorrência com JPA/Hibernate

> Projeto acadêmico para demonstração dos problemas de concorrência em sistemas transacionais
> e a solução com **Locking Otimista** usando `@Version` do JPA/Hibernate.

---

## Integrantes

| Juan Pablo Mescouto da Silva |
|-------|-----------------|
| **Aluno A** | Parte 1 — Entidade `ContaBancaria` sem controle de concorrência |
| **Aluno B** | Parte 2 — Entidade `ContaBancariaVersionada` com `@Version` |

---

## Tecnologias

- Java 17
- Spring Boot 3.3
- Spring Web (REST)
- Spring Data JPA / Hibernate
- Banco de dados H2 (em memória)
- Apache JMeter (testes de carga)

---

## Como Rodar a Aplicação

### Pré-requisitos
- Java 17+ instalado
- Maven 3.6+ instalado

### 1. Clonar o repositório
```bash
git clone https://github.com/JuanSilva078/bancodb.git
cd bancodb
```

### 2. Executar com Maven
```bash
./mvnw spring-boot:run
```
> No Windows: `mvnw.cmd spring-boot:run`

### 3. Acessar a aplicação
- API: `http://localhost:8080`
- Console H2: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:bancodb`
  - User: `sa` | Senha: *(vazia)*

---

## Endpoints da API

### Parte 1 — Sem Controle de Concorrência

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/contas/{id}` | Consulta conta pelo ID |
| `POST` | `/contas/{id}/deposito` | Deposita valor na conta |
| `POST` | `/contas/{id}/saque` | Saca valor da conta |

### Parte 2 — Com Locking Otimista (`@Version`)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/contas-versionadas/{id}` | Consulta conta versionada pelo ID |
| `POST` | `/contas-versionadas/{id}/deposito` | Deposita (com proteção de versão) |
| `POST` | `/contas-versionadas/{id}/saque` | Saca (com proteção de versão) |

### Exemplos de Requisição (curl)

```bash
# Depósito
curl -X POST http://localhost:8080/contas/1/deposito \
     -H "Content-Type: application/json" \
     -d '{"valor": 100.00}'

# Saque
curl -X POST http://localhost:8080/contas/1/saque \
     -H "Content-Type: application/json" \
     -d '{"valor": 50.00}'

# Depósito versionado
curl -X POST http://localhost:8080/contas-versionadas/1/deposito \
     -H "Content-Type: application/json" \
     -d '{"valor": 100.00}'
```

---

## Testes de Concorrência com JMeter

O arquivo `jmeter-concorrencia.jmx` na raiz do projeto contém dois grupos de threads:

- **Thread Group 1 — Sem Controle** (`/contas/1/deposito`): simula o problema do Lost Update
- **Thread Group 2 — Com @Version** (`/contas-versionadas/1/deposito`): demonstra a solução

### Configuração dos Testes

| Parâmetro | Valor usado |
|-----------|-------------|
| Número de threads | 50 |
| Ramp-up period | 1 segundo |
| Loop Count | 10 |
| Total de requisições | 500 por grupo |

---

## Relatório de Conclusão

### Parte 1 — O Problema: Lost Update

**Cenário:** saldo inicial = R$ 1.000,00 | 50 threads × 10 depósitos de R$ 10,00
**Resultado esperado:** R$ 1.000 + (500 × R$ 10,00) = **R$ 6.000,00**
**Resultado obtido:** valor incorreto (menor que o esperado) ❌

**Por quê?**
Múltiplas threads leem o mesmo saldo antes de qualquer uma commitar:
```
Thread A lê: R$ 1.000 → calcula R$ 1.010 → salva R$ 1.010
Thread B lê: R$ 1.000 → calcula R$ 1.010 → salva R$ 1.010  ← sobrescreveu A!
```
Um depósito inteiro foi perdido. Isso é o **Lost Update**.

> *(inserir print do Summary Report do JMeter aqui)*

---

### Parte 2 — A Solução: Locking Otimista com @Version

**Cenário:** mesmo teste, mas apontando para `/contas-versionadas/1/deposito`
**Resultado:** algumas requisições retornam **HTTP 409 Conflict**, mas o saldo final é sempre **consistente** 

**Por quê?**
O Hibernate inclui a versão no `WHERE` do `UPDATE`:
```sql
UPDATE conta_bancaria_versionada
SET saldo = ?, version = version + 1
WHERE id = ? AND version = ?   ← se version mudou, 0 linhas afetadas → exceção!
```
Quando duas threads tentam commitar com a mesma `version`, apenas uma vence.
A outra recebe `ObjectOptimisticLockingFailureException` → tratada como HTTP 409.

> *(inserir print do Summary Report do JMeter aqui)*

---

### Comparativo Final

| Aspecto | Sem Controle (Parte 1) | Com @Version (Parte 2) |
|---------|----------------------|----------------------|
| Saldo final consistente | ❌ Não | ✅ Sim |
| Requisições com erro | 0% (mas dados errados) | ~X% com HTTP 409 |
| Lock no banco de dados | Não | Não (otimista) |
| Custo de performance | Baixo | Baixo |
| Integridade dos dados | Comprometida | Garantida |

**Conclusão:** O Locking Otimista é uma solução eficiente para ambientes com **baixa contenção** (poucos conflitos esperados), pois não bloqueia o banco. Em caso de conflito real, ele detecta e rejeita a operação conflitante, garantindo a integridade dos dados sem sacrificar a performance.
