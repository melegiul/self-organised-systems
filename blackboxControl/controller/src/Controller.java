import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {

    private List<List<Integer>> results = new ArrayList<>();
    private final Map<Integer,Integer> bbDim = Map.of(
            0, 3,
            1, 5,
            2, 2,
            3, 10,
            4, 2
    );

    public Controller() {
    }

    public List<List<Integer>> getResults() {
        return results;
    }

    private String generateVector(int iteration, int dimension){
        List<Integer> list = new ArrayList<>(dimension);
        for(int i=0; i<dimension; i++){
            list.add(iteration);
        }
        String vector = list.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(" ", "", ""));
        return vector;// + "\n";
    }

    private void executeBB() throws IOException {
        for(Map.Entry<Integer,Integer> bb: bbDim.entrySet()){
            results.add(new ArrayList());
            String command = String.format("bash -c docker run -i bb -b %d", bb.getKey());
//            String command = "bash -c pwd";
            String[] commandArray = command.split(" ");
            List<String> commandList = Arrays.asList(commandArray);
            ProcessBuilder builder = new ProcessBuilder(commandList);
            builder.redirectInput(ProcessBuilder.Redirect.PIPE);
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = builder.start();
//            OutputStream stdin = process.getOutputStream();
            InputStream stdout = process.getInputStream();
            BufferedReader output = new BufferedReader(new InputStreamReader(stdout));
//            Writer writer = new OutputStreamWriter(process.getOutputStream(), "UTF-8");
            for(int i=1; i<10; i++){
                String vector = generateVector(i, bb.getValue());
//                writer.write(vector);
//                writer.flush();
                process.getOutputStream().write(vector.getBytes());
                process.getOutputStream().flush();
                String line;
                while ((line = output.readLine()) != null){
                    results.get(bb.getKey()).add(Integer.valueOf(line));
                }
            }
        }
    }

    public void execute(){
        try {
            executeBB();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

}
