package com.example;

import com.example.modelo.Pedido;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

public class ConsumidorPedidos {
    private static final String FILA_NORMAL = "pedidos_normal";
    private static final String FILA_URGENTE = "pedidos_urgente";
    private static final MonitorPedidos monitor = MonitorPedidos.getInstance();

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(FILA_NORMAL, true, false, false, null);
        channel.queueDeclare(FILA_URGENTE, true, false, false, null);
        channel.basicQos(1);

        System.out.println(" [*] Aguardando pedidos. Prioridade para urgentes.");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String mensagem = new String(delivery.getBody(), "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            
            try {
                Pedido pedido = mapper.readValue(mensagem, Pedido.class);
                processarPedido(pedido, channel, delivery);
                
                // Atualiza o monitor
                if (pedido.getPrioridade().equalsIgnoreCase("urgente")) {
                    monitor.incrementarUrgente();
                } else {
                    monitor.incrementarNormal();
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar pedido: " + e.getMessage());
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };

        channel.basicConsume(FILA_URGENTE, false, deliverCallback, consumerTag -> {});
        channel.basicConsume(FILA_NORMAL, false, deliverCallback, consumerTag -> {});
    }

    private static void processarPedido(Pedido pedido, Channel channel, Delivery delivery) 
            throws IOException, InterruptedException {
        
        System.out.println(" [x] Processando: " + pedido);
        
        int tempo = pedido.getPrioridade().equalsIgnoreCase("urgente") ? 1000 : 3000;
        Thread.sleep(tempo);
        
        System.out.println(" [x] Concluído: " + pedido.getPrato() + " (Mesa " + pedido.getMesa() + ")");
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }
}