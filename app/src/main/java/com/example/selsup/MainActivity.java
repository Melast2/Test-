package com.example.selsup;


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;


class CrptApi {
    public static void main(String[] args) throws InterruptedException {
        Description description = new Description("123456789");
        List<Products> products = new ArrayList<>();
        products.add(new Products("qwerty", "2020-01-23", "12345", "12345", "12345",
                "2020-01-23", "1234", "1234", "1234"));
        CrptApi crptApi = CrptApiBuilder.build(TimeUnit.SECONDS, 10);
        for (int i = 0; i < 10; i++) {
            Document document = new Document(description, "qwertyh", "1234", "1234",
                    true, "12456", "123456", "12345",
                    "2020-01-23", "123", products, "2020-01-23", "1234");
            document.doc_id = String.valueOf(i);
            crptApi.create(document);
        }
    }

    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Queue<Document> requestQueue;
    private ScheduledExecutorService scheduler;

    private final String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";



    private CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.requestQueue = new LinkedList<>();
    }
    public synchronized void create(Document doc) {
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
            start();
        }
        requestQueue.offer(doc);
        System.out.println("Request added to queue: " + doc);
    }
    private void start() {
        scheduler.scheduleAtFixedRate(this::processQueue, 0, 1, timeUnit);
    }
    private void processQueue() {
        int processedRequests = 0;
        while (!requestQueue.isEmpty() && processedRequests < requestLimit) {
            processRequest(requestQueue.poll());
            processedRequests++;
        }
        if (requestQueue.isEmpty()) {
            scheduler.shutdown();
            scheduler = null;
        }
    }
    private void processRequest(Document doc) {
        try {
            System.out.println("Processing request: " + doc);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(doc)))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static final class CrptApiBuilder {
        public CrptApiBuilder() {
        }

        public static CrptApi build(TimeUnit timeUnit, int requestLimit) {
            if (requestLimit < 1) {
                throw new IllegalArgumentException("Request limit value must be larger than 0. Given request limit is: " + requestLimit);
            }
            return new CrptApi(timeUnit, requestLimit);
        }
    }

    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Products> products;
        private String reg_date;
        private String reg_number;


        public Document(Description description, String doc_id,
                        String doc_status, String doc_type, boolean importRequest,
                        String owner_inn, String participant_inn,
                        String producer_inn, String production_date,
                        String production_type, List<Products> products,
                        String reg_date, String reg_number) {
            this.description = description;
            this.doc_id = doc_id;
            this.doc_status = doc_status;
            this.doc_type = doc_type;
            this.importRequest = importRequest;
            this.owner_inn = owner_inn;
            this.participant_inn = participant_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.production_type = production_type;
            this.products = products;
            this.reg_date = reg_date;
            this.reg_number = reg_number;
        }
        public Document() {
        }
        public String getDoc_id() {
            return doc_id;
        }
        public void setDoc_id(String doc_id) {
            this.doc_id = doc_id;
        }
        public String getDoc_status() {
            return doc_status;
        }
        public void setDoc_status(String doc_status) {
            this.doc_status = doc_status;
        }
        public String getDoc_type() {
            return doc_type;
        }
        public void setDoc_type(String doc_type) {
            this.doc_type = doc_type;
        }
        public boolean isImportRequest() {
            return importRequest;
        }
        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwner_inn() {
            return owner_inn;
        }
        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }
        public String getParticipant_inn() {
            return participant_inn;
        }
        public void setParticipant_inn(String participant_inn) {
            this.participant_inn = participant_inn;
        }
        public String getProducer_inn() {
            return producer_inn;
        }
        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }
        public String getProduction_date() {
            return production_date;
        }
        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }
        public String getProduction_type() {
            return production_type;
        }
        public void setProduction_type(String production_type) {
            this.production_type = production_type;
        }
        public List<Products> getProducts() {
            return products;
        }
        public void setProducts(List<Products> products) {
            this.products = products;
        }
        public String getReg_date() {
            return reg_date;
        }
        public void setReg_date(String reg_date) {
            this.reg_date = reg_date;
        }
        public String getReg_number() {
            return reg_number;
        }
        public void setReg_number(String reg_number) {
            this.reg_number = reg_number;
        }
        @Override
        public String toString() {
            return "Document{" +
                    "doc_id='" + doc_id + '\'' +
                    '}';
        }
    }
    static class Description {
        private String participantInn;
        public String getParticipantInn() {
            return participantInn;
        }
        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }
        public Description(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    static class Products {

        public Products(String certificate, String certificate_date,
                       String certificate_number, String owner_inn,
                       String producer_inn, String production_date, String tnved_code,
                       String uit_code, String uitu_code) {
            this.certificate_document = certificate;
            this.certificate_document_date = certificate_date;
            this.certificate_document_number = certificate_number;
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.tnved_code = tnved_code;
            this.uit_code = uit_code;
            this.uitu_code = uitu_code;
        }

        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
        public String getCertificate_document() {
            return certificate_document;
        }
        public void setCertificate_document(String certificate_document) {
            this.certificate_document = certificate_document;
        }
        public String getCertificate_document_date() {
            return certificate_document_date;
        }
        public void setCertificate_document_date(String certificate_document_date) {
            this.certificate_document_date = certificate_document_date;
        }
        public String getCertificate_document_number() {
            return certificate_document_number;
        }

        public void setCertificate_document_number(String certificate_document_number) {
            this.certificate_document_number = certificate_document_number;
        }
        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }
        public String getProducer_inn() {
            return producer_inn;
        }
        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }
        public String getProduction_date() {
            return production_date;
        }
        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }
        public String getTnved_code() {
            return tnved_code;
        }
        public void setTnved_code(String tnved_code) {
            this.tnved_code = tnved_code;
        }
        public String getUit_code() {
            return uit_code;
        }
        public void setUit_code(String uit_code) {
            this.uit_code = uit_code;
        }
        public String getUitu_code() {
            return uitu_code;
        }
        public void setUitu_code(String uitu_code) {
            this.uitu_code = uitu_code;
        }
    }
}