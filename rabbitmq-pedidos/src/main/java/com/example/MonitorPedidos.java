package com.example;

public class MonitorPedidos {
    private int pedidosNormaisProcessados;
    private int pedidosUrgentesProcessados;

    public synchronized void incrementarNormal() {
        pedidosNormaisProcessados++;
        imprimirRelatorio();
    }

    public synchronized void incrementarUrgente() {
        pedidosUrgentesProcessados++;
        imprimirRelatorio();
    }

    private void imprimirRelatorio() {
        System.out.println("\n=== RELATÓRIO DE PEDIDOS ===");
        System.out.println("Pedidos Normais: " + pedidosNormaisProcessados);
        System.out.println("Pedidos Urgentes: " + pedidosUrgentesProcessados);
        System.out.println("Total: " + (pedidosNormaisProcessados + pedidosUrgentesProcessados) + "\n");
    }

    public static MonitorPedidos getInstance() {
        return MonitorHolder.INSTANCE;
    }

    private static class MonitorHolder {
        private static final MonitorPedidos INSTANCE = new MonitorPedidos();
    }
}