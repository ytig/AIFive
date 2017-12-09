package five;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ai.FiveAi;
import five.Item.OnClickListener;

public class FiveGame extends JFrame implements OnClickListener{
	private static final long serialVersionUID = -3002840641978587013L;
	public static final int chessSize=15; //棋盘大小
	public static final int pieceLength=40; //棋子大小

	private int[][] mArgs; //棋盘数据结构
	private FiveAi mAi; //五子棋ai
	private boolean isInitiative; //玩家是否先攻
	private boolean isBlack; //是否黑方落子（黑方为先攻方）
	private int pieceCount; //落子总数
	private boolean isEnd; //本轮是否结束

	private ImageIcon[] mIcon; //资源图片
	private JButton[][] mButton; //棋格按钮

	private ResultMemory mMemory; //游戏结果统计
	private int winCount; //连胜装逼计数
	private int loseCount; //连输崩溃计数

	public static void main(String[] args) {new FiveGame();}
	public FiveGame()
	{
		super("五子棋");
		int dWidth=16;
		int dHeight=40;
		setSize(chessSize*pieceLength+dWidth, chessSize*pieceLength+dHeight);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		init();
	}
	private void init()
	{
		mArgs=new int[chessSize][chessSize];
		mAi=new FiveAi();
		mIcon=new ImageIcon[3];
		mIcon[0]=new ImageIcon(new ImageIcon("./img/null.png").getImage().getScaledInstance(pieceLength, pieceLength, Image.SCALE_DEFAULT));
		mIcon[1]=new ImageIcon(new ImageIcon("./img/black.png").getImage().getScaledInstance(pieceLength, pieceLength, Image.SCALE_DEFAULT));
		mIcon[2]=new ImageIcon(new ImageIcon("./img/white.png").getImage().getScaledInstance(pieceLength, pieceLength, Image.SCALE_DEFAULT));
		mButton=new JButton[chessSize][chessSize];
		for(int i=0;i<chessSize;i++)
		{
			for(int j=0;j<chessSize;j++)
			{
				Item mItem=new Item(i, j);
				mButton[i][j]=mItem;
				mItem.setOnClickListener(this); //棋格按下事件
				add(mItem);
			}
		}
		mMemory=new ResultMemory();
		winCount=0;
		loseCount=0;
		if(Math.random()<0.5f) reset(true); //第一轮玩家先攻
		else reset(false); //第一轮玩家后攻
	}
	private void reset(boolean initiative)
	{
		isInitiative=initiative; //决定攻守方
		if(isInitiative) setTitle("五子棋-先攻");
		else setTitle("五子棋-后守");
		isBlack=true;
		pieceCount=0;
		isEnd=false;
		for(int i=0;i<chessSize;i++)
		{
			for(int j=0;j<chessSize;j++)
			{
				mArgs[i][j]=FiveAi.NULL_TAG; //清空棋盘数据
				mButton[i][j].setIcon(mIcon[0]); //棋盘恢复无子显示
			}
		}
		if(!isInitiative) ai(); //ai先攻
	}

	@Override
	public void onClick(int x, int y, JButton item) {
		// TODO Auto-generated method stub
		if(isEnd) //结束点击重置棋局
		{
			reset(!isInitiative); //交换攻守方
			return;
		}
		if(mArgs[x][y]!=FiveAi.NULL_TAG) return; //点击处已落子
		if(isInitiative)
		{
			if(!isBlack) return; //ai回合
		}
		else
		{
			if(isBlack) return; //ai回合
		}
		itemDown(x, y); //落子处理
		if(!isEnd) ai(); //未结束则ai落子
	}

	private void ai()
	{
		SwingUtilities.invokeLater(new Runnable() { //分发任务到主线程堆栈

			@Override
			public void run() {
				// TODO Auto-generated method stub
				int []ai=mAi.doAi(mArgs, !isInitiative);
				itemDown(ai[0], ai[1]);
			}
		});
	}

	private void itemDown(int x, int y)
	{
		if(isBlack) //黑方落子
		{
			if(isInitiative) mButton[x][y].setIcon(mIcon[1]); //强制玩家显示黑子
			else mButton[x][y].setIcon(mIcon[2]); //强制ai显示白子
			mArgs[x][y]=FiveAi.BLACK_TAG;
		}
		else //白方落子
		{
			if(isInitiative) mButton[x][y].setIcon(mIcon[2]); //强制ai显示白子
			else mButton[x][y].setIcon(mIcon[1]); //强制玩家显示黑子
			mArgs[x][y]=FiveAi.WHITE_TAG;
		}
		isBlack=!isBlack; //交换方阵
		pieceCount++; //落子个数计数
		int winResult=win(x, y); //游戏结束判断
		switch(winResult)
		{
			case 1: //胜负已分
				if((isInitiative&&!isBlack)||(!isInitiative&&isBlack)) showResult(1);
				else showResult(-1);
				isEnd=true;
				break;
			case -1: //平局
				showResult(0);
				isEnd=true;
				break;
		}
	}
	private void showResult(int result)
	{
		mMemory.addResult(result, pieceCount, isInitiative);
		switch(result)
		{
			case 1: //赢
				loseCount=0;
				winCount++;
				if(winCount>=100)
				{
					JOptionPane.showMessageDialog(null, "您是机器学习吗？", "", JOptionPane.QUESTION_MESSAGE);
					return;
				}
				if(winCount>=5)
				{
					JOptionPane.showMessageDialog(null, "大神，饶命！", "", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				if(winCount==4)
				{
					JOptionPane.showMessageDialog(null, "什。。什么！", "", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				if(pieceCount>=60)
				{
					JOptionPane.showMessageDialog(null, "真是一场精彩的对决", "", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				if(winCount==3)
				{
					JOptionPane.showMessageDialog(null, "再。。再给我一次机会", "", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				if(winCount==2)
				{
					JOptionPane.showMessageDialog(null, "居然能连赢在下，唔。。报上大名", "", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				JOptionPane.showMessageDialog(null, "是你赢了", "", JOptionPane.INFORMATION_MESSAGE);
				break;
			case -1: //输
				winCount=0;
				loseCount++;
				if(pieceCount<=10)
				{
					JOptionPane.showMessageDialog(null, "你在绕开我走子？", "", JOptionPane.QUESTION_MESSAGE);
					return;
				}
				if(loseCount>=5)
				{
					JOptionPane.showMessageDialog(null, "切！", "", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(loseCount==4)
				{
					JOptionPane.showMessageDialog(null, "赢你就像碾死一只蚂蚁！", "", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(pieceCount>=60)
				{
					JOptionPane.showMessageDialog(null, "你已经做得不错啦，哼哼", "", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(loseCount==3)
				{
					JOptionPane.showMessageDialog(null, "真是轻而易举", "", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(loseCount==2)
				{
					JOptionPane.showMessageDialog(null, "请认真一点吧", "", JOptionPane.ERROR_MESSAGE);
					return;
				}
				JOptionPane.showMessageDialog(null, "你输了噢", "", JOptionPane.ERROR_MESSAGE);
				break;
			case 0: //平
				JOptionPane.showMessageDialog(null, "平局", "", JOptionPane.WARNING_MESSAGE);
				break;
		}
	}

	private int win(int x, int y)
	{
		if(getItemCount(x,y,0,1)+getItemCount(x,y,0,-1)+1>=5) return 1; //游戏结束
		if(getItemCount(x,y,1,0)+getItemCount(x,y,-1,0)+1>=5) return 1; //游戏结束
		if(getItemCount(x,y,1,1)+getItemCount(x,y,-1,-1)+1>=5) return 1; //游戏结束
		if(getItemCount(x,y,1,-1)+getItemCount(x,y,-1,1)+1>=5) return 1; //游戏结束
		if(pieceCount>=chessSize*chessSize) return -1; //平局
		return 0; //未分胜负
	}
	private int getItemCount(int x, int y, int i, int j)
	{
		int itemCount=0;
		int mType=mArgs[x][y];
		int readX=x+i;
		int readY=y+j;
		while(true)
		{
			if(readX<0||readX>=chessSize||readY<0||readY>=chessSize) break;
			if(mType==mArgs[readX][readY])
			{
				itemCount++;
				readX+=i;
				readY+=j;
			}
			else break;
		}
		return itemCount; //返回该方向累计同色棋数
	}
}

class Item extends JButton
{
	private static final long serialVersionUID = 542410627125114506L;
	private int mX;
	private int mY;
	private OnClickListener mListener;

	public Item(int i, int j)
	{
		mX=i;
		mY=j;
		setBounds(new Rectangle(FiveGame.pieceLength*i, FiveGame.pieceLength*j, FiveGame.pieceLength, FiveGame.pieceLength));
		setBorderPainted(false); //去掉边框
		setBackground(new Color(238, 238, 238)); //图片未加载时的按钮样式
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if(mListener!=null) mListener.onClick(mX, mY, Item.this);
			}
		});
	}
	public void setOnClickListener(OnClickListener listener)
	{
		mListener=listener;
	}

	public interface OnClickListener
	{
		public void onClick(int x, int y, JButton item);
	}
}