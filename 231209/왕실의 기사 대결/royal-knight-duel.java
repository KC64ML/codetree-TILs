import java.util.*;
import java.io.*;

public class Main {

    private static class Attacker{
        public final int r;
        public final int c;
        public final int h;
        public final int w;
        public final int k;

        Attacker(int r, int c, int h, int w, int k){
            this.r = r;
            this.c = c;
            this.h = h;
            this.w = w;
            this.k = k;
        }

        public Attacker update(int r, int c, int k){
            return new Attacker(r, c, this.h, this.w, k);
        }
    }

    private static int L, N, Q; // L : 체스판 크기, N : 기사 갯수, Q : 왕의 명령
    private static int[][] graph;
    private static int[][] attackerLocGraph;
    private static ArrayList<Attacker> attackerList;
    private static int[] dx = {0, 1, 0, -1};
    private static int[] dy = {-1, 0, 1, 0};
    private static int[] stamina;
    private static int[] mainStamina;

    
    private static boolean isWithInRange(int row, int col){
        return 0 <= row && 0 <= col && row < L && col < L;
    }

    private static int isInHereUserInex(int curIndex, int row, int col){
        for(int i = 0; i < attackerList.size(); i++){
            if(curIndex == i) continue;
            if(stamina[i] <= 0) continue;

            int startRow = attackerList.get(i).r;
            int startCol = attackerList.get(i).c;
            int endRow = startRow + attackerList.get(i).h - 1;
            int endCol = startCol + attackerList.get(i).w - 1;
            
            if(startRow <= row && startCol <= col && row <= endRow && col <= endCol) return i;
        }

        return -1;
    }

    private static void pushAttacker(int index, int d){
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> userIndexSet = new HashSet<>();
        userIndexSet.add(index);
        queue.add(index);
        
        // System.out.println("사용자 범위 : " + L + " " + L);
        while(queue.size() > 0){
            int curIndex = queue.poll();

            Attacker attacker = attackerList.get(curIndex);
            int nextStartRow = attacker.r + dy[d];
            int nextStartCol = attacker.c + dx[d];
            int nextEndRow = nextStartRow + attacker.h - 1;
            int nextEndCol = nextStartCol + attacker.w - 1;

            // System.out.println("현재 사용자 index : " + curIndex);
            // System.out.println("시작 : " + nextStartRow + " " + nextStartCol + " 끝 : " + nextEndRow + " " + nextEndCol);

            // 시작의 범위가 넘어섰을 경우, 끝의 범위가 넘어섰을 경우
            if(!isWithInRange(nextStartRow, nextStartCol) || !isWithInRange(nextEndRow, nextEndCol)){
                // System.out.println("범위를 벗어나서 종료하겠습니다.");
                return;
            }

            int r1 = 0;
            int r2 = 0;
            int c1 = 0;
            int c2 = 0;

            if(d == 0){
                r1 = nextStartRow;
                r2 = nextStartRow + 1;
                c1 = nextStartCol;
                c2 = nextEndCol;
            }else if(d == 1){
                r1 = nextStartRow;
                r2 = nextEndRow;
                c1 = nextEndCol - 1;
                c2 = nextEndCol;
            }else if(d == 2){
                r1 = nextEndRow - 1;
                r2 = nextEndRow;
                c1 = nextStartCol;
                c2 = nextEndCol;
            }else if(d == 3){
                r1 = nextStartRow;
                r2 = nextEndRow;
                c1 = nextStartCol;
                c2 = nextStartCol + 1;
            }

            for(int row = r1; row <= r2; row++){
                for(int col = c1; col <= c2; col++){

                    // 벽이라면 종료
                    if(graph[row][col] == 2){
                        // System.out.println("벽이라서 종료하겠습니다.");
                        return;
                    } 

                    // 현재 행열 값 중 다른 기사가 있는지 확인
                    int userIndex = isInHereUserInex(curIndex, row, col);
                    if(userIndex < 0) continue;

                    if(stamina[userIndex] <= 0) continue;

                    // 다른 기사 존재할 때, list에 저장
                    userIndexSet.add(userIndex);
                    queue.add(userIndex);
                }
            }

        }
        

        // System.out.println("시작 위치를 변경하겠습니다." + " 크기 : " + userIndexSet.size());
        // Set에 저장되어 있는 사용자 id를 기준으로 시작 위치 변경하기
        for(int userIndex : userIndexSet){
            Attacker attacker = attackerList.get(userIndex);
            int nextStartRow = attacker.r + dy[d];
            int nextStartCol = attacker.c + dx[d];
            int nextEndRow = nextStartRow + attacker.h - 1;
            int nextEndCol = nextStartCol + attacker.w - 1;
            
            int bumpCnt = 0;

            for(int row = nextStartRow; row <= nextEndRow; row++){
                for(int col = nextStartCol; col <= nextEndCol; col++){
                    if(graph[row][col] == 1) bumpCnt++;
                }
            }
            
            // 시작 위치가 아닌 경우 기사 체력 변경
            int nextK = attacker.k - bumpCnt;
            if(index == userIndex) nextK = attacker.k;

            stamina[userIndex] = nextK;

            // 시작 위치 변경
            attackerLocGraph[attacker.r][attacker.c] = -1;

            if(stamina[userIndex] > 0) attackerLocGraph[nextStartRow][nextStartCol] = userIndex;
            
            attackerList.set(userIndex, attackerList.get(userIndex).update(nextStartRow, nextStartCol, nextK));
        }
    }

    private static void gameStation(int index, int direction){
        pushAttacker(index, direction);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer tokenizer = new StringTokenizer(reader.readLine());

        L = Integer.parseInt(tokenizer.nextToken());
        N = Integer.parseInt(tokenizer.nextToken());
        Q = Integer.parseInt(tokenizer.nextToken());

        graph = new int[L][L];
        attackerLocGraph = new int[L][L];
        attackerList = new ArrayList<>();
        mainStamina = new int[N];
        stamina = new int[N];

        for(int i = 0; i < L; i++){
            tokenizer = new StringTokenizer(reader.readLine(), " ");
            for(int j = 0; j < L; j++){
                graph[i][j] = Integer.parseInt(tokenizer.nextToken());
                attackerLocGraph[i][j] = -1;
            }
        }

        for(int i = 0; i < N; i++){
            tokenizer = new StringTokenizer(reader.readLine());
            int r = Integer.parseInt(tokenizer.nextToken()) - 1;
            int c = Integer.parseInt(tokenizer.nextToken()) - 1;
            int h = Integer.parseInt(tokenizer.nextToken());
            int w = Integer.parseInt(tokenizer.nextToken());
            int k = Integer.parseInt(tokenizer.nextToken());

            attackerLocGraph[r][c] = i;
            mainStamina[i] = k;
            stamina[i] = k;
            attackerList.add(new Attacker(r, c, h, w, k));
        }

        for(int i = 0; i < Q; i++){
            tokenizer = new StringTokenizer(reader.readLine());

            int index = Integer.parseInt(tokenizer.nextToken()) - 1;
            int direction = Integer.parseInt(tokenizer.nextToken());
            
            // if(stamina[index] <= 0) continue;
            // System.out.println("시작");
            gameStation(index, direction);

            // for(int row = 0; row < L; row++){
            //     for(int col = 0; col < L; col++){
            //         System.out.print(attackerLocGraph[row][col] + " ");
            //     }
            //     System.out.println();
            // }
            // System.out.println("종료 \n");
        }

        
        // for(int row = 0; row < L; row++){
        //     for(int col = 0; col < L; col++){
        //         System.out.print(graph[row][col] + " ");
        //     }
        //     System.out.println();
        // }

        int answer = 0;

        for(int i = 0; i < N; i++){
            // System.out.print(stamina[i] + " ");
            if(stamina[i] <= 0) continue;
            answer += (mainStamina[i] - stamina[i]);
        }

        // System.out.println();

        System.out.println(answer);

        reader.close();
    }
}