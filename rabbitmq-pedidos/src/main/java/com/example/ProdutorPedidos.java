package com.example;

import com.example.modelo.Pedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;

public class ProdutorPedidos {
    private static final String FILA_NORMAL = "pedidos_normal";
    private static final String FILA_URGENTE = "pedidos_urgente";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            
            channel.queueDeclare(FILA_NORMAL, true, false, false, null);
            channel.queueDeclare(FILA_URGENTE, true, false, false, null);

            ObjectMapper mapper = new ObjectMapper();
            String csvFile = Paths.get("pedidos.csv").toString();

            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String linha;
                boolean cabecalho = true;
                int totalPedidos = 0;
                
                while ((linha = br.readLine()) != null) {
                    if (cabecalho) {
                        cabecalho = false;
                        continue;
                    }

                    String[] dados = linha.split(",");
                    if (dados.length == 3) {
                        Pedido pedido = new Pedido(
                            dados[0].trim(),
                            Integer.parseInt(dados[1].trim()),
                            dados[2].trim()
                        );

                        String mensagem = mapper.writeValueAsString(pedido);
                        String fila = pedido.getPrioridade().equalsIgnoreCase("urgente") 
                            ? FILA_URGENTE : FILA_NORMAL;

                        channel.basicPublish("", fila, null, mensagem.getBytes());
                        System.out.println(" [x] Enviado para " + fila + ": " + mensagem);
                        totalPedidos++;
                    }
                }
                System.out.println("\nTotal de pedidos enviados: " + totalPedidos);
            }
        }
    }
}