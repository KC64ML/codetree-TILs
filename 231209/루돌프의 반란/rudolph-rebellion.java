import java.util.*;
import java.io.*;

public class Main {

    private static class Santa{
        public final int x;
        public final int y;
        public final int index;
        public final boolean nuck;

        Santa(int x, int y, int index, boolean nuck){
            this.x = x;
            this.y = y;
            this.index = index;
            this.nuck = nuck;
        }

        public Santa updateLoc(int x, int y, boolean nuck) {
            return new Santa(x, y, this.index, nuck);
        }

        public Santa updateNuck() {
            boolean changeNuck = (this.nuck == true ? false : true);
            return new Santa(this.x, this.y, this.index, changeNuck);
        }
    }

    private static class Node implements Comparable<Node>{
        public final int x;
        public final int y;
        public final int street;

        Node(int x, int y, int street){
            this.x = x;
            this.y = y;
            this.street = street;
        }

        @Override
        public int compareTo(Node node){
            // 거리가 큰 것
            // 거리가 같다면 r좌표 큰 것
            // r좌표가 같다면 c좌표가 큰 것
            if(this.street != node.street){
                return node.street - this.street;
            }else{
                if(this.y != node.y){
                    return node.y - this.y;
                }else{
                    return node.x - this.x;
                }
            }
        }
    }

    private static int N, M, P, C, D;
    private static int lodolfR;
    private static int lodolfC;
    private static ArrayList<Santa> santaList;
    private static int[] santaState;
    private static int[][] graph;
    private static int[] dx = {0, 1, 0, -1};
    private static int[] dy = {-1, 0, 1, 0};

    private static int[][] dxy = {
        {-1, 0}, {-1, -1}, {0, -1}, {1, -1},
        {1, 0}, {1, 1}, {0, 1}, {-1, 1}
    }; // 8방면

    private static boolean isWithInRange(int row, int col){
        return 0 <= row && 0 <= col && row < N && col < N;
    }

    private static int[][] testCopy(){
        int[][] testCase = new int[N][N];

        for(int i = 0; i < N; i++){
            testCase[i] = graph[i].clone();
        }

        return testCase;
    }

    private static void moveSantaLocation(int moveSantaRow, int moveSantaCol, int addRow, int addCol){
        int nextRow = moveSantaRow;
        int nextCol = moveSantaCol;

        int[][] testCase = testCopy();

        while(true){
            if(graph[nextRow][nextCol] == 0) break;
            if(!isWithInRange(nextRow + addRow, nextCol + addCol)){
                // 해당 산타는 범위를 나갔기에 아웃
                santaState[graph[nextRow][nextCol]] = 0;
                break;
            }
            graph[nextRow + addRow][nextCol + addCol] = testCase[nextRow][nextCol];
            nextRow = nextRow + addRow;
            nextCol = nextCol + addCol;
        }
    }
    // 루돌프 움직임
    private static void lodolMoving(){
        Queue<Node> queue = new LinkedList<>();
        queue.add(new Node(lodolfR, lodolfC, 0));
        boolean[][] visited = new boolean[N][N];
        visited[lodolfR][lodolfC] = true;
        int totalStreet = Integer.MAX_VALUE;
        PriorityQueue<Node> pq = new PriorityQueue<>();

        while(queue.size() > 0){
            Node node = queue.poll();

            if(totalStreet < node.street) continue;

            if(graph[node.y][node.x] > 0 && totalStreet > node.street){
                // 거리를 비교한다.
                pq.add(new Node(node.x, node.y, node.street));
                totalStreet = node.street;
                continue;
            }

            for(int i = 0; i < 8; i++){
                int nx = dxy[i][0] + node.x;
                int ny = dxy[i][1] + node.y;

                if(!isWithInRange(ny, nx)) continue;
                if(visited[ny][nx]) continue;
                visited[ny][nx] = true;
                queue.add(new Node(nx, ny, node.street + 1));
            }
        }

        // 루돌프 위치 이동
        Node node = pq.poll();

        // (a - b ? 0 : (a - b) / Math.abs(a - b));
        int addRow = (node.y - lodolfR == 0  ? 0 : (node.y - lodolfR / Math.abs(node.y - lodolfR)));
        int addCol = (node.x - lodolfC == 0 ? 0 : (node.x - lodolfC / Math.abs(node.x - lodolfC)));

        lodolfR += addRow;
        lodolfC += addCol;

        // 산타 루돌프 역방향으로 점프
        if(graph[lodolfR][lodolfC] > 0){
            int moveSantaRow = addRow * C + lodolfR;
            int moveSantaCol = addCol * C + lodolfC;

            // 범위를 벗어났다면
            if(!isWithInRange(moveSantaRow, moveSantaCol)){
                santaState[graph[lodolfR][lodolfC]] = 0;
                graph[lodolfR][lodolfC] = 0;
            }else {
                // 범위 안이라면 위치 이동
                Santa curSanta = santaList.get(graph[lodolfR][lodolfC]).updateLoc(moveSantaRow, moveSantaCol, true);
                santaList.set(graph[lodolfR][lodolfC], curSanta);
                santaState[graph[lodolfR][lodolfC]] += C;

                moveSantaLocation(moveSantaRow, moveSantaCol, addRow, addCol);
                graph[moveSantaRow][moveSantaCol] = graph[lodolfR][lodolfC];
                graph[lodolfR][lodolfC] = 0;
            }
        }

        graph[lodolfR][lodolfC] = -10;
                
    }

    private static void santaMoving(){
        // 1 ~ P까지
        for(int i = 0; i < P; i++){
            Santa santa = santaList.get(i);

            // 산타가 종료된 경우
            if(santaState[santa.index - 1] == 0) continue;
            if(santa.nuck == true){
                // 산타가 현재 기절한 상태
                santaList.set(i, santa.updateNuck());
            }else{
                // 거리
                int minDist = (int)Math.pow(lodolfR - santa.y, 2) + (int)Math.pow(lodolfC - santa.x, 2);
                int direction = -1;

                // 현재 총 길이보다 길면 안 움직인다.
                for(int k = 0; k < 4; k++){
                    int nx = dx[k] + santa.x;
                    int ny = dy[k] + santa.y;

                    if(!isWithInRange(ny, nx)) continue;
                    if(graph[ny][nx] > 0) continue;

                    int dist = (int)Math.pow(lodolfR - ny, 2) + (int)Math.pow(lodolfC - nx, 2);
                    if(dist < minDist){
                        minDist = dist;
                        direction = k;
                    }
                }

                if(direction != -1){
                    int nx = dx[direction] + santa.x;
                    int ny = dy[direction] + santa.y;

                    // 루돌프가 있다면 반대 방향으로
                    if(graph[ny][nx] == -10){
                        santaState[santa.index - 1] += D;
                        nx = nx + dx[(direction + 2) % 4] * D;
                        ny = ny + dy[(direction + 2) % 4] * D;

                        if(!isWithInRange(ny, nx)) santaState[santa.index - 1] = 0;
                        else{
                            moveSantaLocation(ny, nx, dy[(direction + 2) % 4], dx[(direction + 2) % 4]);
                            graph[ny][nx] = graph[santa.y][santa.x];
                            graph[santa.y][santa.x] = 0;
                        }
                    }
                }

            }

        }
    }

    private static void gameStation(){
        // (1) 루돌프 움직임
        lodolMoving();

        // (2) 산타 움직임
        santaMoving();
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tokenizer = new StringTokenizer(reader.readLine());

        N = Integer.parseInt(tokenizer.nextToken());
        M = Integer.parseInt(tokenizer.nextToken());
        P = Integer.parseInt(tokenizer.nextToken());
        C = Integer.parseInt(tokenizer.nextToken());
        D = Integer.parseInt(tokenizer.nextToken());

        tokenizer = new StringTokenizer(reader.readLine());
        lodolfR = Integer.parseInt(tokenizer.nextToken()) - 1;
        lodolfC = Integer.parseInt(tokenizer.nextToken()) - 1;

        // 루돌프 : -10
        graph = new int[N][N];
        graph[lodolfR][lodolfC] = -10;

        santaState = new int[P];
        santaList = new ArrayList<>();

        for(int i = 0; i < P; i++){
            tokenizer = new StringTokenizer(reader.readLine());
            int p = Integer.parseInt(tokenizer.nextToken());
            int sR = Integer.parseInt(tokenizer.nextToken()) - 1;
            int sC = Integer.parseInt(tokenizer.nextToken()) - 1;

            graph[sR][sC] = p;
            santaList.add(new Santa(sC, sR, p, false));
        }

        // 인덱스 기준으로 정렬
        Collections.sort(santaList, (s1, s2) -> s1.index - s2.index);

        gameStation();

        System.out.println(Arrays.toString(santaState));

        reader.close();
    }
}