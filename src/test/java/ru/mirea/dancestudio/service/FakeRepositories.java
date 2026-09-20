package ru.mirea.dancestudio.service;

import ru.mirea.dancestudio.exception.EntityNotFoundException;
import ru.mirea.dancestudio.model.Client;
import ru.mirea.dancestudio.model.DanceClass;
import ru.mirea.dancestudio.model.Enrollment;
import ru.mirea.dancestudio.repository.ClientRepository;
import ru.mirea.dancestudio.repository.DanceClassRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Хранилища в памяти для модульных тестов: тот же интерфейс, что у JDBC-репозиториев (полиморфизм),
 * поэтому сервисы проверяются без базы данных. Хранилища отдают копии, как настоящая БД.
 */
final class FakeRepositories {

    private FakeRepositories() {
    }

    static Client copy(Client c) {
        return new Client(c.getId(), c.getFullName(), c.getPhone(), c.getEmail(), c.getBirthDate(),
                c.getRegisteredAt());
    }

    static DanceClass copy(DanceClass d) {
        return new DanceClass(d.getId(), d.getTitle(), d.getStyle(), d.getLevel(), d.getInstructor(),
                d.getStartTime(), d.getDurationMinutes(), d.getCapacity(), d.getMinAge(), d.getPrice());
    }

    static Enrollment copy(Enrollment e) {
        return new Enrollment(e.getId(), copy(e.getClient()), copy(e.getDanceClass()), e.getStatus(),
                e.getPrice(), e.isPaid(), e.getNote(), e.getCreatedAt());
    }

    static final class Clients implements ClientRepository {
        private final Map<Long, Client> data = new LinkedHashMap<>();
        private long sequence = 0;

        @Override
        public Client save(Client client) {
            client.setId(++sequence);
            data.put(client.getId(), copy(client));
            return client;
        }

        @Override
        public Optional<Client> findById(long id) {
            return Optional.ofNullable(data.get(id)).map(FakeRepositories::copy);
        }

        @Override
        public List<Client> findAll() {
            return data.values().stream().map(FakeRepositories::copy).toList();
        }

        @Override
        public void update(Client client) {
            if (!data.containsKey(client.getId())) {
                throw EntityNotFoundException.client(client.getId());
            }
            data.put(client.getId(), copy(client));
        }

        @Override
        public boolean deleteById(long id) {
            return data.remove(id) != null;
        }

        @Override
        public long count() {
            return data.size();
        }

        @Override
        public Optional<Client> findByPhone(String phone) {
            return data.values().stream().filter(c -> c.getPhone().equals(phone)).findFirst().map(FakeRepositories::copy);
        }

        @Override
        public Optional<Client> findByEmail(String email) {
            return data.values().stream().filter(c -> c.getEmail().equals(email)).findFirst().map(FakeRepositories::copy);
        }

        @Override
        public List<Client> search(String term) {
            String q = term.toLowerCase();
            return data.values().stream()
                    .filter(c -> c.getFullName().toLowerCase().contains(q) || c.getPhone().contains(q)
                            || c.getEmail().contains(q))
                    .map(FakeRepositories::copy).toList();
        }
    }

    static final class Classes implements DanceClassRepository {
        private final Map<Long, DanceClass> data = new LinkedHashMap<>();
        private long sequence = 0;

        @Override
        public DanceClass save(DanceClass danceClass) {
            danceClass.setId(++sequence);
            data.put(danceClass.getId(), copy(danceClass));
            return danceClass;
        }

        @Override
        public Optional<DanceClass> findById(long id) {
            return Optional.ofNullable(data.get(id)).map(FakeRepositories::copy);
        }

        @Override
        public List<DanceClass> findAll() {
            return new ArrayList<>(data.values().stream().map(FakeRepositories::copy).toList());
        }

        @Override
        public void update(DanceClass danceClass) {
            if (!data.containsKey(danceClass.getId())) {
                throw EntityNotFoundException.danceClass(danceClass.getId());
            }
            data.put(danceClass.getId(), copy(danceClass));
        }

        @Override
        public boolean deleteById(long id) {
            return data.remove(id) != null;
        }

        @Override
        public long count() {
            return data.size();
        }
    }
}
