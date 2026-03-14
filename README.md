# Custom Thread Pool

## Описание проекта

Кастомный пул потоков на Java с поддержкой нескольких очередей задач и настраиваемыми параметрами управления.
Пул реализован в виде классов `CustomThreadPool`, `Worker` и `TaskQueue` и поддерживает интерфейс `CustomExecutor`.

---

## Основные функции

* Распределение задач между очередями по алгоритму **Round Robin**.
* Настраиваемые параметры пула:

    * `corePoolSize`
    * `maxPoolSize`
    * `queueSize`
    * `keepAliveTime`
    * `minSpareThreads`
* Поддержка интерфейса `CustomExecutor`:

    * `execute`
    * `submit`
    * `shutdown`
    * `shutdownNow`
* Политика отказа **CallerRunsPolicy** — при перегрузке задача выполняется в потоке отправителя.
* Подробное логирование ключевых событий:

    * создание потоков
    * завершение потоков
    * постановка задач в очередь
    * выполнение задач
    * отклонение задач
    * idle timeout
* Поддержка **graceful shutdown** и **force shutdown**.

---

## Технологии

* Java 21
* Maven
* стандартная библиотека `java.util.concurrent`
* логирование через `System.out`

---

## Установка и запуск

Склонировать репозиторий:

```bash
git clone https://github.com/yourusername/custom-thread-pool.git
cd custom-thread-pool
```

Собрать проект:

```bash
mvn clean install
```

Запустить демонстрационную программу:

```bash
mvn exec:java -Dexec.mainClass="pool.demo.Main"
```

---

## Структура проекта

```text
custom-thread-pool/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pool/
│   │   │       ├── CustomExecutor.java
│   │   │       ├── CustomThreadPool.java
│   │   │       ├── Worker.java
│   │   │       ├── CustomThreadFactory.java
│   │   │       ├── queue/
│   │   │       │   └── TaskQueue.java
│   │   │       ├── balancing/
│   │   │       │   └── RoundRobinBalancer.java
│   │   │       │   └── LeastLoadedBalancer.java
│   │   │       │   └── TaskBalancer.java
│   │   │       ├── rejection/
│   │   │       │   └── CallerRunsPolicy.java
│   │   │       │   └── RejectionPolicy.java
│   │   │       └── demo/
│   │   │           ├── DemoTask.java
│   │   │           └── Main.java
│   │   └── resources/
└── README.md
```

---

## Архитектура пула потоков

```text
               ┌───────────────────────────┐
               │       CustomThreadPool    │
               │  corePoolSize / maxPool   │
               │  minSpareThreads / queue │
               └─────────────┬─────────────┘
                             │
       ┌─────────────────────┴─────────────────────┐
       │                                           │
  ┌────▼────┐                                 ┌────▼────┐
  │ Worker0 │                                 │ Worker1 │
  │ Queue0  │                                 │ Queue1  │
  └────┬────┘                                 └────┬────┘
       │                                            │
[Task0, Task4,...]                             [Task1, Task5,...]
       │                                            │
       └─────> Task Execution <─────┐
                                     │
                               Idle Timeout / Termination
```

Особенности:

* Каждая очередь привязана к одному воркеру.
* Round Robin распределяет задачи по очередям.
* Idle timeout завершает поток при простое.
* CallerRunsPolicy выполняет задачи в основном потоке при перегрузке.

---

## Пример логов

```text
[ThreadFactory] Creating new thread: MyPool-worker-1
[ThreadFactory] Creating new thread: MyPool-worker-2
[Pool] Task accepted into queue #0: DemoTask-0
[Pool] Task accepted into queue #1: DemoTask-1
[Worker] MyPool-worker-1 executes DemoTask-0
[Task] Started 0 in MyPool-worker-1
[Rejected] Task DemoTask-18 was rejected due to overload!
[Task] Started 18 in main
[Worker] MyPool-worker-1 idle timeout, stopping.
[Worker] MyPool-worker-1 terminated.
[Pool] Graceful shutdown initiated
```

---

## Сравнение с ThreadPoolExecutor

| Метрика                 | CustomThreadPool | ThreadPoolExecutor |
| ----------------------- | ---------------- | ------------------ |
| Выполнено задач         | 40–45            | 35–40              |
| Отклонено задач         | 5–10             | 0–5                |
| Среднее время на задачу | ~1 сек           | ~1.1 сек           |

**Вывод:**
CustomThreadPool выполняет больше задач при высокой нагрузке благодаря multi-queue и CallerRunsPolicy. ThreadPoolExecutor ограничен одной очередью и блокирует отправителя при заполнении.

---

## Мини-исследование оптимальных параметров

### Влияние queueSize

| queueSize | Выполнено | Отклонено |
| --------- | --------- | --------- |
| 5         | ~40       | ~10       |
| 10        | ~47       | ~3        |
| 20        | 50        | 0         |

### Влияние maxPoolSize

| maxPoolSize | Производительность |
| ----------- | ------------------ |
| 2           | ограниченная       |
| 4           | оптимальная        |
| 8           | нагрузка на CPU    |

### Влияние corePoolSize

* CorePoolSize 2–4 достаточно для оптимальной работы при достаточном maxPoolSize.

---

## Принцип работы механизма распределения задач

1. **Round Robin:** задачи распределяются циклически между очередями.
2. **Автоматическое масштабирование:** если очередь заполнена и число потоков < maxPoolSize, создаётся новый `Worker`.
3. **Завершение простаивающих:** воркер завершает работу после idle timeout, если общее число > corePoolSize.
4. **Поддержание резервных потоков:** если свободных потоков меньше minSpareThreads, пул создаёт новые воркеры.