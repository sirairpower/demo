package com.cacafly.exam.demo.middle.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Service
public class MiniGameService {

    public void startGame(){

    }

    class Recorder{

        public final int START_SCORE = 0;
        public final int END_SCORE = 100;
        private Map<RaceRunner,Integer> ranking;
        private Map<RaceRunner,Integer> raceRecord;
        Recorder(Map<RaceRunner,Integer> _raceRecord){
            raceRecord = _raceRecord;
            ranking = new ConcurrentHashMap<>();
        }

        synchronized public Integer readRecord(RaceRunner raceRunner){
            return raceRecord.get(raceRunner);
        }
        synchronized public boolean writeRecord(RaceRunner raceRunner,int newStep){
            boolean isWrite = false;
            int moveDistance = readRecord(raceRunner);
            if(needMoreStep(moveDistance)){
                raceRecord.put(raceRunner,moveDistance+newStep);
                isWrite = true;
            }else{
                //決定名次
                if(!ranking.containsKey(raceRunner)){
                    ranking.put(raceRunner,ranking.size()+1);
                }
            }
            return isWrite;
        }
        public void register(RaceRunner raceRunner){
            raceRecord.put(raceRunner,START_SCORE);
        }
        boolean needMoreStep(int moveDistance){
            return moveDistance<END_SCORE;
        }
        synchronized boolean isGameFinished(){
            return ranking.size()>=raceRecord.size();
        }
        Map<RaceRunner,Integer> getRaceRecord(){
            return raceRecord;
        }
        synchronized void report(){
            System.out.println(" ");
            getRaceRecord().forEach((k,v)->{
                System.out.println(k.name+":"+v);
            });
            System.out.println(" ");
            if(isGameFinished()){
                System.out.println(" --- ranking --- ");
                ranking.forEach((k,v)->{
                    System.out.println(k.name+":"+v);
                });
                System.out.println(" --------------- ");
            }
        }
    }

    class RunningGame{

        private final int racerNumber;
        private ScheduledExecutorService executorService;
        private Recorder recorder;

        RunningGame(List<String> runners){
            racerNumber = runners.size();
            executorService = Executors.newScheduledThreadPool(racerNumber);
            recorder = new Recorder(new ConcurrentHashMap<>(racerNumber));
            runners.forEach(r->{
                recorder.register(new RaceRunner(r,this));
            });

        }

        Recorder getRecorder(){
            return recorder;
        }

        void StartGame(){
            while(!recorder.isGameFinished()){
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recorder.getRaceRecord().forEach((k,v)->{
                    if(recorder.isGameFinished()){
                        executorService.shutdownNow();
                    }
                    if(recorder.needMoreStep(v)){
                        executorService.scheduleAtFixedRate(k,0,1000L,TimeUnit.MILLISECONDS);
                    }
                });
            }
            if(!executorService.isShutdown()){
                executorService.shutdownNow();
                executorService = null;
            }

        }
    }

    class RaceRunner implements Runnable{

        private String name;
        private RunningGame game;
//        private long delay = 100L;


        RaceRunner(String _name,RunningGame _game){
            name = _name;
            game = _game;
        }

        @Override
        public void run() {
            game.getRecorder().writeRecord(this,oneStep());
            game.getRecorder().report();
        }

        public Integer oneStep(){
            return ThreadLocalRandom.current().nextInt(1,5);
        }

        @Override
        public String toString() {
            return "RaceRunner{" +
                    "name='" + name + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RaceRunner that = (RaceRunner) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {

            return Objects.hash(name);
        }

    }

    public static void main(String... args) throws ExecutionException, InterruptedException {
        MiniGameService mgs = new MiniGameService();
        List<String> runners = Arrays.asList(new String[]{"r1","r2","r3"});
        RunningGame runningGame = mgs.new RunningGame(runners);
        runningGame.StartGame();
    }

}
