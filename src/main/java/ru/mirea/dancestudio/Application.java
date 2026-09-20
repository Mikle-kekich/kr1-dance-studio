package ru.mirea.dancestudio;

import ru.mirea.dancestudio.exception.DanceStudioException;
import ru.mirea.dancestudio.exception.InputClosedException;
import ru.mirea.dancestudio.repository.ClientRepository;
import ru.mirea.dancestudio.repository.DanceClassRepository;
import ru.mirea.dancestudio.repository.EnrollmentRepository;
import ru.mirea.dancestudio.repository.JdbcClientRepository;
import ru.mirea.dancestudio.repository.JdbcDanceClassRepository;
import ru.mirea.dancestudio.repository.JdbcDatabaseInfoRepository;
import ru.mirea.dancestudio.repository.JdbcEnrollmentRepository;
import ru.mirea.dancestudio.service.ClientService;
import ru.mirea.dancestudio.service.DanceClassService;
import ru.mirea.dancestudio.service.DatabaseInfoService;
import ru.mirea.dancestudio.service.EnrollmentQueryService;
import ru.mirea.dancestudio.service.EnrollmentService;
import ru.mirea.dancestudio.service.ExportService;
import ru.mirea.dancestudio.service.StatisticsService;
import ru.mirea.dancestudio.ui.ClientMenu;
import ru.mirea.dancestudio.ui.ConsoleInput;
import ru.mirea.dancestudio.ui.DanceClassMenu;
import ru.mirea.dancestudio.ui.EnrollmentMenu;
import ru.mirea.dancestudio.ui.ExportMenu;
import ru.mirea.dancestudio.ui.FilterMenu;
import ru.mirea.dancestudio.ui.MainMenu;
import ru.mirea.dancestudio.ui.Printer;
import ru.mirea.dancestudio.ui.SearchMenu;
import ru.mirea.dancestudio.ui.SortMenu;
import ru.mirea.dancestudio.util.DatabaseManager;

import java.io.PrintStream;
import java.time.Clock;
import java.util.List;

/**
 * Сборка приложения («корень композиции»): создаёт репозитории, сервисы и меню и связывает их.
 * Слои зависят друг от друга только через конструкторы: Console UI -> Service -> Repository -> БД.
 */
public class Application {

    private static final String INIT_DB_FLAG = "--init-db";

    private final ConsoleInput in;
    private final PrintStream out;

    public Application(ConsoleInput in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    /** @return код завершения: 0 - обычная работа, 1 - программу не удалось запустить */
    public int run(String[] args) {
        boolean forceInit = List.of(args).contains(INIT_DB_FLAG);
        try {
            DatabaseManager db = DatabaseManager.fromConfiguration();
            if (!new DatabaseBootstrap(db, in, out).prepare(forceInit)) {
                return 1;
            }
            buildMainMenu(db).run();
            out.println("До свидания!");
            return 0;
        } catch (InputClosedException e) {
            out.println();
            out.println("Ввод завершён. Работа программы остановлена.");
            return 0;
        } catch (DanceStudioException e) {
            out.println("Критическая ошибка: " + e.getMessage());
            return 1;
        }
    }

    private MainMenu buildMainMenu(DatabaseManager db) {
        Clock clock = Clock.systemDefaultZone();

        ClientRepository clientRepository = new JdbcClientRepository(db);
        DanceClassRepository classRepository = new JdbcDanceClassRepository(db);
        EnrollmentRepository enrollmentRepository = new JdbcEnrollmentRepository(db);

        ClientService clientService = new ClientService(clientRepository, enrollmentRepository, clock);
        DanceClassService classService = new DanceClassService(classRepository, enrollmentRepository, clock);
        EnrollmentService enrollmentService = new EnrollmentService(
                enrollmentRepository, clientRepository, classRepository, clock);
        EnrollmentQueryService queryService = new EnrollmentQueryService(enrollmentRepository, clientRepository);
        StatisticsService statisticsService = new StatisticsService(
                clientRepository, classRepository, enrollmentRepository, clock);
        ExportService exportService = new ExportService(
                clientService, classService, enrollmentService, statisticsService);
        DatabaseInfoService infoService = new DatabaseInfoService(new JdbcDatabaseInfoRepository(db));

        Printer printer = new Printer(out, clock);
        return new MainMenu(in, out, printer,
                new ClientMenu(in, out, printer, clientService),
                new DanceClassMenu(in, out, printer, classService),
                new EnrollmentMenu(in, out, printer, enrollmentService, clientService, classService),
                new SearchMenu(in, out, printer, queryService),
                new FilterMenu(in, out, printer, queryService),
                new SortMenu(in, out, printer, queryService),
                new ExportMenu(in, out, printer, exportService),
                statisticsService, infoService);
    }
}
