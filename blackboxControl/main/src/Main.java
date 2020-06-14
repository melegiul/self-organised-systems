import java.util.List;

public class Main {
    public static void main(String[] args){
        Controller bbController = new Controller();
        bbController.execute();
        for(List<Integer> list: bbController.getResults()){
            for(Integer i: list){
                System.out.println(i + "\t");
            }
            System.out.println("\n");
        }
    }
}
