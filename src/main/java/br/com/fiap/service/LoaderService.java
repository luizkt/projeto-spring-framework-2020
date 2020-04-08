package br.com.fiap.service;

import br.com.fiap.entity.Student;
import br.com.fiap.entity.Transaction;
import br.com.fiap.repository.StudentRepository;
import br.com.fiap.repository.TransactionRepository;
import br.com.fiap.utils.NameFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoaderService {

    private final StudentRepository studentRepository;
    private final TransactionRepository transactionRepository;

    public LoaderService(StudentRepository studentRepository, TransactionRepository transactionRepository) {
        this.studentRepository = studentRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional()
    public ResponseEntity<String> loadFromCsv() {

        try {

            List<Student> students = csvReaderStudent();
            List<Transaction> transactions = csvReaderTransaction(students);

            studentRepository.saveAll(students);
            transactionRepository.saveAll(transactions);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
            String body = "{\"message\":\"Added all the students and transactions successfully\"}";

            return new ResponseEntity<>(body, headers, HttpStatus.CREATED);

        } catch (Exception e) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString());
            String body = "{\"message\":\"An error has occurred\", \"exception\":" + e.getMessage() + "}";

            return new ResponseEntity<>(body, headers, HttpStatus.BAD_REQUEST);
        }
    }

    private List<Student> csvReaderStudent() throws IOException {
        List<Student> students = new ArrayList<>();

        BufferedReader csvReader = new BufferedReader(new FileReader("./files/lista_alunos.csv"));
        String row;
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(";");
            Student student = new Student(Integer.parseInt(data[0]), NameFormatter.capitalizeName(data[1]));

            students.add(student);
        }
        csvReader.close();
        return students;
    }

    private List<Transaction> csvReaderTransaction(List<Student> students) throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        BufferedReader csvReader = new BufferedReader(new FileReader("./files/lista_transacoes.csv"));
        String row;
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(";");
            Transaction transaction = new Transaction(Integer.parseInt(data[0]), Integer.parseInt(data[1]), data[2], Double.parseDouble(data[3]), data[4]);

            transaction.setStudent(
                    students.stream()
                            .filter(student -> transaction.getStudentRegistrationNumber().equals(student.getStudentRegistrationNumber()))
                            .findAny()
                            .orElse(null));

            transactions.add(transaction);
        }
        csvReader.close();
        return transactions;
    }
}