package ai;

public class FiveAi {
    public static final int NULL_TAG=0; //无子标记
    public static final int BLACK_TAG=1; //黑子标记
    public static final int WHITE_TAG=2; //白子标记
    private boolean beast=false; //狂暴进攻模式（仅进行最低限度的防守）

    /**
     * 五子棋Ai
     * @param args 未分胜负的棋盘
     * @param isBlack 我方是否为黑方
     * @return
     */
    public int[] doAi(int [][]args, boolean isBlack)
    {
        int myTag,otherTag;
        if(isBlack)
        {
            myTag=BLACK_TAG;
            otherTag=WHITE_TAG;
        }
        else
        {
            myTag=WHITE_TAG;
            otherTag=BLACK_TAG;
        }
        int directionI,directionJ,startI,startJ,directionDi,directionDj; //随机遍历方向
        if(Math.random()<1/2f)
        {
            directionI=1;
            startI=0;
        }
        else
        {
            directionI=-1;
            startI=args.length-1;
        }
        if(Math.random()<1/2f)
        {
            directionJ=1;
            startJ=0;
        }
        else
        {
            directionJ=-1;
            startJ=args[0].length-1;
        }
        if(Math.random()<1/2f) directionDi=1;
        else directionDi=-1;
        if(Math.random()<1/2f) directionDj=1;
        else directionDj=-1;
        float []parameterWin=new float[]{0.1f, 1f, 2f}; //权比参数
        int [][]coordinatesAD=new int[6][2]; //必胜攻防落点
        for(int i=0;i<coordinatesAD.length;i++)
        {
            for(int j=0;j<coordinatesAD[0].length;j++) coordinatesAD[i][j]=-1;
        }
        float []maxWeightsD=new float[coordinatesAD.length/2]; //必胜防守采用权比替换
        boolean [][]attackWin=new boolean[args.length][args[0].length]; //已进攻标记
        boolean [][]defenceWin=new boolean[args.length][args[0].length]; //已防守标记
        int nullCount=0; //盘内空格数，判断棋局重开，重置状态标记
        int defaultX=-1,defaultY=-1; //默认坐标，优先居中
        for(int i=startI;i>=0&&i<args.length;i+=directionI)
        {
            for(int j=startJ;j>=0&&j<args[0].length;j+=directionJ)
            {
                if(args[i][j]==myTag)
                {
                    int range=2; //以己方落点范围搜索进攻点
                    for(int di=-range*directionDi;Math.abs(di)<=range;di+=directionDi)
                    {
                        for(int dj=-range*directionDj;Math.abs(dj)<=range;dj+=directionDj)
                        {
                            int nX=i+di;
                            int nY=j+dj;
                            if(nX<0||nX>=args.length||nY<0||nY>=args[0].length) continue;
                            if((!attackWin[nX][nY])&&args[nX][nY]==NULL_TAG) //搜索点无子且不重复搜索
                            {
                                int n=0;
                                while(true) //逐步预测，先入为主
                                {
                                    if(2*n>=coordinatesAD.length) break;
                                    if(coordinatesAD[2*n][0]!=-1) break;
                                    if(isWinByStep(args, nX, nY, isBlack, n+1)) //n+1步预测
                                    {
                                        coordinatesAD[2*n][0]=nX;
                                        coordinatesAD[2*n][1]=nY;
                                        break;
                                    }
                                    n++;
                                }
                                attackWin[nX][nY]=true; //标记搜索完成
                            }
                        }
                    }
                }
                if(args[i][j]==otherTag)
                {
                    int range=2; //以对方落点范围搜索防守点
                    for(int di=-range*directionDi;Math.abs(di)<=range;di+=directionDi)
                    {
                        for(int dj=-range*directionDj;Math.abs(dj)<=range;dj+=directionDj)
                        {
                            int nX=i+di;
                            int nY=j+dj;
                            if(nX<0||nX>=args.length||nY<0||nY>=args[0].length) continue;
                            if((!defenceWin[nX][nY])&&args[nX][nY]==NULL_TAG) //搜索点无子且不重复搜索
                            {
                                int n=0;
                                while(true) //逐步预测，权比替换
                                {
                                    if(2*n+1>=coordinatesAD.length) break;
                                    if(isWinByStep(args, nX, nY, !isBlack, n+1)) //n+1步预测
                                    {
                                        float nWeight=extendWeight(args, nX, nY, !isBlack, parameterWin);
                                        if(nWeight>=maxWeightsD[n])
                                        {
                                            coordinatesAD[2*n+1][0]=nX;
                                            coordinatesAD[2*n+1][1]=nY;
                                            maxWeightsD[n]=nWeight;
                                        }
                                    }
                                    if(coordinatesAD[2*n+1][0]!=-1) break;
                                    n++;
                                }
                                defenceWin[nX][nY]=true; //标记搜索完成
                            }
                        }
                    }
                }
                if(args[i][j]==NULL_TAG) //设置默认落点
                {
                    nullCount++; //无子计数
                    if(defaultX==-1) //保证落点存在
                    {
                        defaultX=i;
                        defaultY=j;
                    }
                    else //优先居中，受随机遍历方向影响
                    {
                        if(Math.abs(args.length-1-2*i)+Math.abs(args[0].length-1-2*j)<Math.abs(args.length-1-2*defaultX)+Math.abs(args[0].length-1-2*defaultY))
                        {
                            defaultX=i;
                            defaultY=j;
                        }
                    }
                }
            }
        }
        if(nullCount+1>=args.length*args[0].length) beast=false; //棋盘重开，关闭狂暴模式
        int [][]block=null;
        if(!beast)
        {
            block=unstableBlock(args, isBlack); //延伸攻防搜索区域，先寻找强势块
            if(block!=null) beast=true; //存在强势块，进入狂暴模式
            else block=unstableBlock(args, !isBlack); //后寻找弱势块
        }
        if(block==null) block=args; //全局搜索
        float []parameterExtend=new float[]{0.1f, 1f, 2f}; //权比参数
        if(beast) parameterExtend[2]=10f; //狂暴模式下必拦加权
        int []coordinateA=new int[]{-1, -1}; //延伸进攻点
        float maxWeightA=0f; //现延伸进攻点权比
        int mCoinA=1; //同权等概率替换运算计数
        boolean [][]attackExtend=new boolean[block.length][block[0].length]; //延伸进攻标记
        int []coordinateD=new int[]{-1, -1}; //延伸防守点
        float maxWeightD=0f; //现延伸防守点权比
        int mCoinD=1; //同权等概率替换运算计数
        boolean [][]defenceExtend=new boolean[block.length][block[0].length]; //延伸防守标记
        boolean neverDown=true; //第一手落子判断
        int otherX=-1,otherY=-1; //对方某落子坐标
        for(int i=startI;i>=0&&i<block.length;i+=directionI)
        {
            for(int j=startJ;j>=0&&j<block[0].length;j+=directionJ)
            {
                if(block[i][j]==myTag)
                {
                    neverDown=false; //非第一手落子
                    int range=2; //以己方落点范围搜索延伸进攻点
                    for(int di=-range*directionDi;Math.abs(di)<=range;di+=directionDi)
                    {
                        for(int dj=-range*directionDj;Math.abs(dj)<=range;dj+=directionDj)
                        {
                            int nX=i+di;
                            int nY=j+dj;
                            if(nX<0||nX>=block.length||nY<0||nY>=block[0].length) continue;
                            if((!attackExtend[nX][nY])&&args[nX][nY]==NULL_TAG) //搜索点无子且不重复搜索
                            {
                                float nWeight=extendWeight(args, nX, nY, isBlack, parameterExtend);
                                if(nWeight==maxWeightA&&nWeight>0) //同权等概率替换
                                {
                                    if(Math.random()<1f/(mCoinA+1)) //硬币法则
                                    {
                                        coordinateA[0]=nX;
                                        coordinateA[1]=nY;
                                    }
                                    mCoinA++;
                                }
                                if(nWeight>maxWeightA) //替换高连通性数据
                                {
                                    coordinateA[0]=nX;
                                    coordinateA[1]=nY;
                                    maxWeightA=nWeight;
                                    mCoinA=1;
                                }
                                attackExtend[nX][nY]=true; //标记搜索完成
                            }
                        }
                    }
                }
                if(block[i][j]==otherTag)
                {
                    if(otherX==-1) //记录对方落点，用于后手特殊处理
                    {
                        otherX=i;
                        otherY=j;
                    }
                    int range=2; //以对方落点范围搜索延伸防守点
                    for(int di=-range*directionDi;Math.abs(di)<=range;di+=directionDi)
                    {
                        for(int dj=-range*directionDj;Math.abs(dj)<=range;dj+=directionDj)
                        {
                            int nX=i+di;
                            int nY=j+dj;
                            if(nX<0||nX>=block.length||nY<0||nY>=block[0].length) continue;
                            if((!defenceExtend[nX][nY])&&args[nX][nY]==NULL_TAG) //搜索点无子且不重复搜索
                            {
                                float nWeight=extendWeight(args, nX, nY, !isBlack, parameterExtend);
                                if(nWeight==maxWeightD&&nWeight>0) //同权等概率替换
                                {
                                    if(Math.random()<1f/(mCoinD+1)) //硬币法则
                                    {
                                        coordinateD[0]=nX;
                                        coordinateD[1]=nY;
                                    }
                                    mCoinD++;
                                }
                                if(nWeight>maxWeightD) //替换高连通性数据
                                {
                                    coordinateD[0]=nX;
                                    coordinateD[1]=nY;
                                    maxWeightD=nWeight;
                                    mCoinD=1;
                                }
                                defenceExtend[nX][nY]=true; //标记搜索完成
                            }
                        }
                    }
                }
            }
        }
        if(beast) //最低限度防守模式，不进行多步预测防守，不进行延伸防守
        {
            for(int step=3;2*step-1<coordinatesAD.length;step++) coordinatesAD[2*step-1][0]=-1;
            maxWeightD=0;
        }
        for(int i=0;i<coordinatesAD.length;i++)
        {
            if(coordinatesAD[i][0]!=-1) //返回最高权必胜落点
            {
                return new int[]{coordinatesAD[i][0], coordinatesAD[i][1]};
            }
        }
        if(neverDown)
        {
            if(otherX!=-1) //后手第一手落子，特殊处理
            {
                int paddingFollow=0; //跟随落子外距（块理论引入，不推荐不跟随落子）
                int paddingRandom=6; //随机落子外距（边沿优先向中心方向落子）
                if(otherX>=0+paddingFollow&&otherX<block.length-paddingFollow&&otherY>=0+paddingFollow&&otherY<block[0].length-paddingFollow)
                {
                    int dX=0,dY=0; //跟随落子方向
                    do {
                        if(otherX>=0+paddingRandom&&otherX<block.length-paddingRandom)  dX=(int)(3*Math.random())-1; //随机赋值
                        else //向心赋值
                        {
                            if(2*otherX<block.length-1) dX=1;
                            else
                            {
                                if(2*otherX==block.length-1)
                                {
                                    if(Math.random()<1/2f) dX=1;
                                    else dX=-1;
                                }
                                else dX=-1;
                            }
                        }
                        if(otherY>=0+paddingRandom&&otherY<block[0].length-paddingRandom) dY=(int)(3*Math.random())-1; //随机赋值
                        else //向心赋值
                        {
                            if(2*otherY<block[0].length-1) dY=1;
                            else
                            {
                                if(2*otherY==block[0].length-1)
                                {
                                    if(Math.random()<1/2f) dY=1;
                                    else dY=-1;
                                }
                                else dY=-1;
                            }
                        }
                    }while(dX==0&&dY==0); //随机失败
                    if(args[otherX+dX][otherY+dY]==NULL_TAG) return new int[]{otherX+dX, otherY+dY};
                }
                return new int[]{defaultX, defaultY}; //返回默认落点
            }
        }
        if(maxWeightA!=0||maxWeightD!=0) //返回最高权落点
        {
            if(maxWeightA>maxWeightD) return coordinateA;
            else
            {
                if(maxWeightA==maxWeightD&&isBlack) return coordinateA; //先手更倾向进攻
                return coordinateD;
            }
        }
        return new int[]{defaultX, defaultY}; //返回默认落点
    }

    /**
     * 必胜判断
     * @param args 未分胜负的棋盘
     * @param mX 我方此次落点横坐标
     * @param mY 我方此次落点纵坐标
     * @param isBlack 我方是否为黑方
     * @param step 我方可行步数
     * @return
     */
    protected boolean isWinByStep(int [][]args, int mX, int mY, boolean isBlack, int step)
    {
        Range mRange=null; //迭代方法不可缺省遍历范围
        switch(step)
        {
            case 1:
                return isWinByOne(args, mX, mY, isBlack, mRange);
            case 2:
                return isWinByTwo(args, mX, mY, isBlack, mRange);
            case 3:
                mRange=new Range(args, mX, mY, 0); //设置正预测间距可加速迭代遍历，但会影响精确度
                return isWinByThree(args, mX, mY, isBlack, mRange);
        }
        return false;
    }
    /**
     * n步必胜迭代方法
     * @param args 未分胜负的棋盘
     * @param mX 我方此次落点横坐标
     * @param mY 我方此次落点纵坐标
     * @param isBlack 我方是否为黑方
     * @param mRange 遍历范围，由棋盘结构（包括新落点）和预测间距决定
     * @param iteration n-1步必胜方法
     * @return
     */
    private boolean isWinByIteration(int [][]args, int mX, int mY, boolean isBlack, Range mRange, Iteration iteration)
    {
        if(iteration.iterationFunc(args, mX, mY, isBlack, mRange)) return true; //n-1步内已可必胜（可用1步方法替代）
        int myTag,otherTag;
        if(isBlack)
        {
            myTag=BLACK_TAG;
            otherTag=WHITE_TAG;
        }
        else
        {
            myTag=WHITE_TAG;
            otherTag=BLACK_TAG;
        }
        args[mX][mY]=myTag; //设置落子供子方法调用，避免新建数组，返回前需置空保证数组未变
        boolean hasNull=false; //是否满盘，使用局部范围不影响满盘判断
        for(int x1=mRange.startX;x1<mRange.endX;x1++)
        {
            for(int y1=mRange.startY;y1<mRange.endY;y1++)
            {
                if(args[x1][y1]!=NULL_TAG) continue; //已经有子
                hasNull=true;
                Range range1=mRange.getChildById(1, mRange, x1, y1); //范围根据新落点拓宽
                if(iteration.iterationFunc(args, x1, y1, !isBlack, range1)) //对方落子此处必胜，此时我方必输（可用1步方法替代）
                {
                    args[mX][mY]=NULL_TAG; //返回前置空
                    return false;
                }
                args[x1][y1]=otherTag; //设置新落子供子方法调用
                boolean hasWin=false; //对于对方任何走法，我方需存在必胜应对策略
                for(int x2=range1.startX;x2<range1.endX;x2++)
                {
                    for(int y2=range1.startY;y2<range1.endY;y2++)
                    {
                        if(args[x2][y2]!=NULL_TAG) continue;
                        Range range2=mRange.getChildById(2, range1, x2, y2); //范围需再次拓宽
                        if(iteration.iterationFunc(args, x2, y2, isBlack, range2)) hasWin=true; //我方落子此处即必胜应对策略
                        if(hasWin) break;
                    }
                    if(hasWin) break;
                }
                args[x1][y1]=NULL_TAG; //二层遍历后置空新落子
                if(!hasWin) //不存在必胜应对策略
                {
                    args[mX][mY]=NULL_TAG; //返回前置空
                    return false;
                }
            }
        }
        args[mX][mY]=NULL_TAG; //返回前置空
        if(!hasNull) return false; //由于我方n-1步内已经无法必胜，此情况会导致和棋（未满盘会进入以上二层遍历）
        return true;
    }
    /**
     * 一步棋内是否必得胜利
     * @param args 未分胜负的棋盘
     * @param mX 我方此次落点横坐标
     * @param mY 我方此次落点纵坐标
     * @param isBlack 我方是否为黑方
     * @param mRange 遍历范围，由棋盘结构（包括新落点）和预测间距决定
     * @return
     */
    private boolean isWinByOne(int [][]args, int mX, int mY, boolean isBlack, Range mRange)
    {
        int [][]status=extendStatus(args, mX, mY, isBlack, 2, 0); //连续落子数一般有限，因此不设置边缘上限
        for(int i=0;i<status.length;i++)
        {
            if(status[i][0]+status[i][1]>=4) return true;
        }
        return false;
    }
    /**
     * 两步棋内是否必得胜利（近似算法替代减少迭代量）
     * @param args 未分胜负的棋盘
     * @param mX 我方此次落点横坐标
     * @param mY 我方此次落点纵坐标
     * @param isBlack 我方是否为黑方
     * @param mRange 遍历范围，由棋盘结构（包括新落点）和预测间距决定
     * @return
     */
    private boolean isWinByTwo(int [][]args, int mX, int mY, boolean isBlack, Range mRange)
    {
        int [][]status=extendStatus(args, mX, mY, isBlack, 4, 1); //设置边缘上限，防止延伸至棋盘边缘，提高效率
        for(int i=0;i<status.length;i++)
        {
            if(status[i][0]+status[i][1]>=4) return true;
            if((status[i][0]+status[i][1]>=3)&&status[i][2]>=1&&status[i][3]>=1) return true;
        }
        return false;
    }
    /**
     * 三步棋内是否必得胜利
     * @param args 未分胜负的棋盘
     * @param mX 我方此次落点横坐标
     * @param mY 我方此次落点纵坐标
     * @param isBlack 我方是否为黑方
     * @param mRange 遍历范围，由棋盘结构（包括新落点）和预测间距决定
     * @return
     */
    private boolean isWinByThree(int [][]args, int mX, int mY, boolean isBlack, Range mRange)
    {
        return isWinByIteration(args, mX, mY, isBlack, mRange, new Iteration() {

            @Override
            public boolean iterationFunc(int[][] args, int mX, int mY, boolean isBlack, Range mRange) {
                // TODO Auto-generated method stub
                return isWinByTwo(args, mX, mY, isBlack, mRange);
            }
        });
    }

    /**
     * 寻找不稳定强势块，不存在返回空
     * @param args 未分胜负的棋盘
     * @param isBlack 我方是否为黑方
     * @return
     */
    protected int[][] unstableBlock(int [][]args, boolean isBlack)
    {
        int myTag;
        if(isBlack) myTag=BLACK_TAG;
        else myTag=WHITE_TAG;
        int [][]block=null;
        for(int i=0;i<args.length;i++)
        {
            for(int j=0;j<args[0].length;j++)
            {
                if(args[i][j]==myTag) block=_unstableBlock(args, i, j);
                if(block!=null) return block;
            }
        }
        return block;
    }
    private int[][] _unstableBlock(int [][]args, int mX, int mY) //扩张不稳定强势块
    {
        int myTag=args[mX][mY];
        int [][]block=new int[args.length][args[0].length];
        boolean [][]expand=new boolean[args.length][args[0].length]; //已扩张标记
        block[mX][mY]=myTag;
        int myCount=1; //块内我方棋子数量
        int otherCount=0; //块内对方棋子数量
        while(true)
        {
            boolean needContinue=false;
            for(int i=0;i<block.length;i++)
            {
                for(int j=0;j<block[0].length;j++)
                {
                    if((!expand[i][j])&&block[i][j]!=NULL_TAG)
                    {
                        int range=2; //以落点范围扩张块
                        for(int di=-range;di<=range;di++)
                        {
                            for(int dj=-range;dj<=range;dj++)
                            {
                                int nX=i+di;
                                int nY=j+dj;
                                if(nX<0||nX>=block.length||nY<0||nY>=block[0].length) continue;
                                if(block[nX][nY]==NULL_TAG&&args[nX][nY]!=NULL_TAG)
                                {
                                    block[nX][nY]=args[nX][nY];
                                    if(block[nX][nY]==myTag) myCount++;
                                    else otherCount++;
                                    if(myCount+otherCount>10) return null; //规模过大为僵持块
                                }
                            }
                        }
                        expand[i][j]=true;
                        needContinue=true;
                    }
                    if(needContinue) break;
                }
                if(needContinue) break;
            }
            if(!needContinue) break;
        }
        if(myCount>otherCount) return block; //判断是否为强势块
        return null;
    }

    /**
     * 计算落点延伸权比
     * @param args 未分胜负的棋盘
     * @param mX 我方此次落点横坐标
     * @param mY 我方此次落点纵坐标
     * @param isBlack 我方是否为黑方
     * @param weight 权比参数（依次为浮动比例，小权比，大权比）
     * @return
     */
    protected float extendWeight(int [][]args, int mX, int mY, boolean isBlack, float []weight)
    {
        float sumWeight=0;
        int [][]status=extendStatus(args, mX, mY, isBlack, 8, 0); //落点方向计数
        for(int n=0;n<status.length;n++) //1权增子必拦，2权必拦，全部方向权比叠加
        {
            float weightLeft=0f;
            if(status[n][2]==1) weightLeft=_extendWeight(new int[]{status[n][0], status[n][1], status[n][2], status[n][3], status[n][4], status[n][5], status[n][6], status[n][7]}, weight);
            float weightRight=0f;
            if(status[n][3]==1) weightRight=_extendWeight(new int[]{status[n][1], status[n][0], status[n][3], status[n][2], status[n][5], status[n][4], status[n][7], status[n][6]}, weight);
            if(weightLeft!=0||weightRight!=0) //单孔结构带权
            {
                if(weightLeft>weightRight) sumWeight+=weightLeft;
                else sumWeight+=weightRight;
                continue;
            }
            int gapLeft=status[n][2]+status[n][4]+status[n][6];
            int gapRight=status[n][3]+status[n][5]+status[n][7];
            switch(status[n][0]+status[n][1]+1)
            {
                case 2: //连续两子
                    if(gapLeft!=0&&gapRight!=0) //无拦截
                    {
                        if(gapLeft>=2&&gapRight>=2) //空隙过量，多向拓展
                        {
                            sumWeight+=weight[1]*(1+weight[0]);
                            break;
                        }
                        if(gapLeft+gapRight>=4) sumWeight+=weight[1]; //空隙足够
                    }
                    break;
                case 3: //连续三子
                    if(gapLeft==0||gapRight==0) //有拦截
                    {
                        if(gapLeft+gapRight>=2) sumWeight+=weight[1]; //空隙足够
                    }
                    else //无拦截
                    {
                        if(gapLeft==1&&gapRight==1) sumWeight+=weight[1]; //空隙不足
                        else
                        {
                            if(gapLeft>=2&&gapRight>=2) //空隙过量，多向拓展
                            {
                                sumWeight+=weight[2]*(1+weight[0]);
                                break;
                            }
                            sumWeight+=weight[2]; //空隙足够
                        }
                    }
                    break;
                case 4: //连续四子
                    if(gapLeft!=0||gapRight!=0) sumWeight+=weight[2]; //非双边拦截
                    break;
            }
        }
        return sumWeight;
    }
    private float _extendWeight(int []status, float []weight) //计算单孔结构权比
    {
        int gapLeft=status[6];
        int gapRight=status[3]+status[5]+status[7];
        switch(status[0]+status[1]+1)
        {
            case 1: //连续一子
                if(status[4]>=3) return weight[2];  //一拖三
                if(status[4]>=2) //一拖二
                {
                    if(gapLeft==0&&gapRight==0) return 0f; //双边拦截
                    if(gapLeft==0||gapRight==0) return weight[1]; //单边拦截
                    return weight[2]; //无拦截
                }
                if(status[4]>=1) //一拖一
                {
                    if(gapLeft==0||gapRight==0) return 0f; //有拦截
                    if(gapLeft+gapRight>=3) return weight[1]*(1-weight[0]); //弱连通性，特殊处理
                }
                break;
            case 2: //连续两子
                if(status[4]>=2) return weight[2]; //二拖二
                if(status[4]>=1) //二拖一
                {
                    if(gapLeft==0&&gapRight==0) return 0f; //双边拦截
                    if(gapLeft==0||gapRight==0) return weight[1]; //单边拦截
                    return weight[2]; //无拦截
                }
                break;
            case 3: //连续三子
                if(status[4]>=1) return weight[2]; //三拖一
                break;
        }
        return 0f;
    }

    /**
     * 统计落点连通数据
     * @param args 未分胜负的棋盘
     * @param mX 我方此次落点横坐标
     * @param mY 我方此次落点纵坐标
     * @param isBlack 我方是否为黑方
     * @param stageCount 统计层数
     * @param edgeRange 边缘计数最大值限制，零表示无限制
     * @return
     */
    protected int[][] extendStatus(int [][]args, int mX, int mY, boolean isBlack, int stageCount, int edgeRange)
    {
        int [][]status=new int[4][stageCount];
        _extendStatus(args, mX, mY, isBlack, status, 0, 0, 0, 1, edgeRange);
        _extendStatus(args, mX, mY, isBlack, status, 0, 1, 0, -1, edgeRange);
        _extendStatus(args, mX, mY, isBlack, status, 1, 0, 1, 0, edgeRange);
        _extendStatus(args, mX, mY, isBlack, status, 1, 1, -1, 0, edgeRange);
        _extendStatus(args, mX, mY, isBlack, status, 2, 0, 1, 1, edgeRange);
        _extendStatus(args, mX, mY, isBlack, status, 2, 1, -1, -1, edgeRange);
        _extendStatus(args, mX, mY, isBlack, status, 3, 0, 1, -1, edgeRange);
        _extendStatus(args, mX, mY, isBlack, status, 3, 1, -1, 1, edgeRange);
        return status;
    }
    private void _extendStatus(int [][]args, int mX, int mY, boolean isBlack, int [][]status, int mI, int dJ, int dX, int dY, int edgeRange) //单向连通数据统计
    {
        int otherTag;
        if(isBlack) otherTag=WHITE_TAG;
        else otherTag=BLACK_TAG;
        int stage=0; //统计阶段
        int extendX=mX+dX;
        int extendY=mY+dY;
        while(true)
        {
            if(extendX<0||extendX>=args.length||extendY<0||extendY>=args[0].length) break; //被边缘拦截停止搜索
            if(args[extendX][extendY]==otherTag) break; //被对方子拦截停止搜索
            boolean changeStage=false;
            if(stage%2==0) //统计同色子数
            {
                if(args[extendX][extendY]!=NULL_TAG)
                {
                    status[mI][2*stage+dJ]++;
                    if(edgeRange>0) //存在边缘计数上限
                    {
                        if(2*(stage+1)>=status[0].length) //正在统计边缘计数
                        {
                            if(status[mI][2*stage+dJ]>=edgeRange) break; //已达边缘需要计数停止搜索
                        }
                    }
                }
                else changeStage=true;
            }
            else //统计气孔数
            {
                if(args[extendX][extendY]==NULL_TAG)
                {
                    status[mI][2*stage+dJ]++;
                    if(edgeRange>0) //存在边缘计数上限
                    {
                        if(2*(stage+1)>=status[0].length) //正在统计边缘计数
                        {
                            if(status[mI][2*stage+dJ]>=edgeRange) break; //已达边缘需要计数停止搜索
                        }
                    }
                }
                else changeStage=true;
            }
            if(changeStage) //切换统计阶段
            {
                stage++;
                if(2*stage+dJ>=status[0].length) break; //统计结束
            }
            else //延伸搜索位
            {
                extendX+=dX;
                extendY+=dY;
            }
        }
    }

    interface Iteration //迭代方法统一接口
    {
        boolean iterationFunc(int [][]args, int mX, int mY, boolean isBlack, Range mRange);
    }
    class Range //迭代遍历范围结构
    {
        public int startX; //局部范围起始横坐标
        public int startY; //局部范围起始纵坐标
        public int endX; //局部范围终止横坐标
        public int endY; //局部范围终止纵坐标
        private int pieceSpace; //预测落子间距，零会遍历全局
        private int lengthX; //横向总长
        private int lengthY; //纵向总长
        private Range range1; //迭代通用子范围
        private Range range2; //迭代通用子范围
        public Range(int [][]args, int mX, int mY, int mSpace) //根据棋盘结构初始化遍历范围
        {
            pieceSpace=mSpace;
            lengthX=args.length;
            lengthY=args[0].length;
            if(pieceSpace<=0)
            {
                startX=0;
                startY=0;
                endX=args.length;
                endY=args[0].length;
                return;
            }
            startX=-1;
            startY=-1;
            endX=-1;
            endY=-1;
            for(int i=0;i<args.length;i++)
            {
                for(int j=0;j<args[0].length;j++)
                {
                    if(args[i][j]!=NULL_TAG||(i==mX&&j==mY))
                    {
                        if(startX==-1) startX=i;
                        endX=i;
                        if(startY==-1) startY=j;
                        if(startY>j) startY=j;
                        if(endY==-1) endY=j;
                        if(endY<j) endY=j;
                    }
                }
            }
            startX-=pieceSpace;
            if(startX<0) startX=0;
            startY-=pieceSpace;
            if(startY<0) startY=0;
            endX+=(1+pieceSpace);
            if(endX>lengthX) endX=lengthX;
            endY+=(1+pieceSpace);
            if(endY>lengthY) endY=lengthY;
        }
        private Range(Range range) //子范围构造方法
        {
            pieceSpace=range.pieceSpace;
            lengthX=range.lengthX;
            lengthY=range.lengthY;
        }
        public Range getChildById(int id, Range range, int nX, int nY) //获取子范围并重新赋值
        {
            if(pieceSpace<=0) return this; //迭代过程中公用全局范围
            if(id==1)
            {
                if(range1==null) range1=new Range(this);
                range1.resetRange(range, nX, nY);
                return range1;
            }
            if(id==2)
            {
                if(range2==null) range2=new Range(this);
                range2.resetRange(range, nX, nY);
                return range2;
            }
            return null;
        }
        private void resetRange(Range range, int nX, int nY) //根据当前局部范围和新落点重新赋值
        {
            startX=range.startX;
            startY=range.startY;
            endX=range.endX;
            endY=range.endY;
            if(startX>nX-pieceSpace) startX=nX-pieceSpace;
            if(startX<0) startX=0;
            if(startY>nY-pieceSpace) startY=nY-pieceSpace;
            if(startY<0) startY=0;
            if(endX<nX+1+pieceSpace) endX=nX+1+pieceSpace;
            if(endX>lengthX) endX=lengthX;
            if(endY<nY+1+pieceSpace) endY=nY+1+pieceSpace;
            if(endY>lengthY) endY=lengthY;
        }
    }
}