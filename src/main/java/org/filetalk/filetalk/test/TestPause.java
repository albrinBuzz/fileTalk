package org.filetalk.filetalk.test;

public class TestPause  {
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    private String mensaje;

    public void run() {
        mensaje="Descargando";
        while (running) {
            System.out.println(mensaje);
            synchronized (pauseLock) {

                if (!running) { // may have changed while waiting to
                    // synchronize on pauseLock
                    break;
                }
                if (paused) {
                    try {
                        System.out.println(mensaje);
                        pauseLock.wait(); // will cause this Thread to block until
                        // another thread calls pauseLock.notifyAll()
                        // Note that calling wait() will
                        // relinquish the synchronized lock that this
                        // thread holds on pauseLock so another thread
                        // can acquire the lock to call notifyAll()
                        // (link with explanation below this code)
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (!running) { // running might have changed since we paused
                        System.out.println(mensaje);
                        break;
                    }
                }
            }
            // Your code here
        }
    }

    public void stop() {
        running = false;
        // you might also want to interrupt() the Thread that is
        // running this Runnable, too, or perhaps call:
        mensaje="parando Descarga";
        resume();
        // to unblock
    }

    public void pause() {
        // you may want to throw an IllegalStateException if !running
        mensaje="pausando Descarga";
        paused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            mensaje="continuado Descarga";
            pauseLock.notifyAll(); // Unblocks thread
        }
    }

    public void sendFile() {
        mensaje="enviado";
        for (; ; ) {

            synchronized (pauseLock) {

                if (!running) { // may have changed while waiting to
                    // synchronize on pauseLock
                    System.out.println(mensaje);
                    break;
                }
                if (paused) {
                    try {
                        System.out.println(mensaje);
                        pauseLock.wait(); // will cause this Thread to block until
                        // another thread calls pauseLock.notifyAll()
                        // Note that calling wait() will
                        // relinquish the synchronized lock that this
                        // thread holds on pauseLock so another thread
                        // can acquire the lock to call notifyAll()
                        // (link with explanation below this code)
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (!running) { // running might have changed since we paused
                        System.out.println(mensaje);
                        break;
                    }
                }else{
                    System.out.println(mensaje);
                }
            }


        }
    }


    public static void main(String[] args) throws InterruptedException {
        TestPause testPause=new TestPause();
        Thread thread=new Thread(testPause::sendFile);
        thread.start();
        Thread.sleep(1);
        testPause.pause();
        Thread.sleep(1);
        testPause.resume();
        Thread.sleep(1);
        testPause.stop();

    }
}