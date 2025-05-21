import java.util.concurrent.*;

public class Celula {
    boolean estado = false;

    Celula arriba;
    Celula abajo;
    Celula izquierda;
    Celula derecha;

    public void setAbajo(Celula abajo) {
        this.abajo = abajo;
    }

    public void setArriba(Celula arriba) {
        this.arriba = arriba;
    }

    public void setDerecha(Celula derecha) {
        this.derecha = derecha;
    }

    public void setIzquierda(Celula izquierda) {
        this.izquierda = izquierda;
    }

    public static Celula[][] grid = new Celula[25][25];

    public static void llenarGrid() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                Celula nuevaCelula = new Celula();
                grid[i][j] = nuevaCelula;
            }
        }
    }

    public static void establecerHermanos() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (i > 0 && i < grid.length - 1) {
                    grid[i][j].setArriba(grid[i + 1][j]);
                    grid[i][j].setAbajo(grid[i - 1][j]);
                } else if (i == 0) {
                    grid[i][j].setAbajo(grid[i + 1][j]);
                    grid[i][j].setArriba(null);
                } else {
                    grid[i][j].setArriba(grid[i - 1][j]);
                    grid[i][j].setAbajo(null);
                }

                if (j > 0 && j < grid[i].length - 1) {
                    grid[i][j].setDerecha(grid[i][j + 1]);
                    grid[i][j].setIzquierda(grid[i][j - 1]);
                } else if (j == 0) {
                    grid[i][j].setDerecha(grid[i][j + 1]);
                    grid[i][j].setIzquierda(null);
                } else {
                    grid[i][j].setDerecha(null);
                    grid[i][j].setIzquierda(grid[i][j - 1]);
                }
            }
        }
    }

    public static void encenderCelula(int y, int x) {
        grid[x][y].estado = true;
    }

    public static void iniciarJuego(int movimientos) {

        //Estado Incial del Juego
        encenderCelula(12, 12);
        encenderCelula(13, 12);
        encenderCelula(14, 12);
        encenderCelula(13, 13);
        encenderCelula(14, 13);
        encenderCelula(14, 15);
        encenderCelula(1, 12);
        encenderCelula(2, 12);
        encenderCelula(3, 12);
        encenderCelula(2, 13);
        encenderCelula(3, 13);
        encenderCelula(3, 15);

        int puntos = 0;

        ExecutorService executor = Executors.newFixedThreadPool(25);
        for (int movs = 0; movs < movimientos; movs++) {
            boolean[][] nuevoEstado = new boolean[grid.length][grid[0].length];
            Future<?>[] futures = new Future<?>[25];
            for (int i = 0; i < 25; i++) {
                final int row = i;
                futures[i] = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j < 25; j++) {
                            int vecinosVivos = contarVecinosVivos(row, j);
                            if (grid[row][j].estado) {
                                if (vecinosVivos < 2 || vecinosVivos > 3) {
                                    nuevoEstado[row][j] = false;
                                } else {
                                    nuevoEstado[row][j] = true;
                                }
                            } else {
                                if (vecinosVivos == 3) {
                                    nuevoEstado[row][j] = true;
                                } else {
                                    nuevoEstado[row][j] = false;
                                }
                            }
                        }
                    }
                });
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            // Aplicar el nuevo estado al tablero
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    grid[i][j].estado = nuevoEstado[i][j];
                }
            }
            puntos += contarPuntos();
            imprimirTablero(puntos);
        }
        executor.shutdown();
    }

    public static void imprimirTablero(int puntos) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j].estado) {
                    System.out.print(" ■ ");
                } else {
                    System.out.print(" · ");
                }
            }
            System.out.println();
        }
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Puntos : " + puntos);

        //Comentar linea para accelerar el juego
        impresionLenta();

    }

    private static void impresionLenta() {
            try {
                Thread.sleep(500); // Espera de 500 milisegundos entre impresiones
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }

    public static int contarVecinosVivos(int x, int y) {
        int count = 0;
        int[][] offsets = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1},           {0, 1},
                {1, -1},  {1, 0},  {1, 1}
        };
        for (int[] offset : offsets) {
            int newX = x + offset[0];
            int newY = y + offset[1];
            if (newX >= 0 && newX < grid.length && newY >= 0 && newY < grid[0].length && grid[newX][newY].estado) {
                count++;
            }
        }
        return count;
    }

    public static int contarPuntos() {
        int puntos = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j].estado) {
                    puntos++;
                }
            }
        }
        return puntos;
    }

    public static void main(String[] args) {
        llenarGrid();
        establecerHermanos();

        long inicio = System.currentTimeMillis();

        iniciarJuego(1000);

        long fin = System.currentTimeMillis();

        long duracion = fin - inicio;
        System.out.println("Duración del juego: " + duracion + " milisegundos");
    }
}

