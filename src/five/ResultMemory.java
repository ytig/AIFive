package five;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ResultMemory
{
	private static final String ROOT_PATH="./data"; //文件存储根目录
	private static final String RESULT_NAME="result.txt"; //游戏结果存储文件
	private static final String STATISTICS_NAME="statistics.txt"; //统计游戏结果展示文件
	private List<Result> mList;

	public ResultMemory()
	{
		mList=new ArrayList<>();
		readFile(); //从文件读取历史结果记录
	}

	public void addResult(int result, int pieceCount, boolean isBlack) //添加新结果
	{
		Result mResult=new Result();
		mResult.result=result;
		mResult.pieceCount=pieceCount;
		mResult.isBlack=isBlack;
		mList.add(mResult);
		writeFile(mResult); //写入结果到文件
	}

	private File createFile(String rootPath, String fileName) //创建文件及其父目录
	{
		boolean noException=true;
		try{
			File rootFile=new File(rootPath);
			if(!rootFile.exists()) rootFile.mkdirs();
			File file=new File(rootPath+"/"+fileName);
			if(!file.exists()) file.createNewFile();
		}catch(Exception e) {noException=false;}
		if(noException) return new File(rootPath+"/"+fileName);
		return null;
	}
	private boolean readFile() //读取数据
	{
		boolean noException=true;
		try{
			FileReader fileReader=new FileReader(createFile(ROOT_PATH, RESULT_NAME));
			BufferedReader bufferedReader=new BufferedReader(fileReader);
			String string=null;
			while((string=bufferedReader.readLine())!=null) mList.add(Result.fromString(string)); //按行读取历史结果
			bufferedReader.close();
			fileReader.close();
		}catch(Exception e) {noException=false;}
		return noException;
	}
	private boolean writeFile(Result result) //写入数据
	{
		boolean noException=true;
		try{
			FileWriter fileWriter=new FileWriter(createFile(ROOT_PATH, RESULT_NAME), true);
			fileWriter.write(Result.toString(result)+"\n"); //按行添加新结果
			fileWriter.close();
		}catch(Exception e) {noException=false;}
		try{
			FileWriter fileWriter=new FileWriter(createFile(ROOT_PATH, STATISTICS_NAME), false);
			fileWriter.write(statisticsResult()); //覆盖统计展示
			fileWriter.close();
		}catch(Exception e) {noException=false;}
		return noException;
	}
	private String statisticsResult() //统计展示数据
	{
		String statistics="";
		statistics+="次数---总计:"+getGameCount(null);
		statistics+=" 先攻:"+getGameCount(true);
		statistics+=" 后守:"+getGameCount(false)+"\r\n";
		statistics+="胜率---总计:"+getWinRatio(null);
		statistics+=" 先攻:"+getWinRatio(true);
		statistics+=" 后守:"+getWinRatio(false);
		return statistics;
	}

	public String getGameCount(Boolean isBlack) //统计游戏次数
	{
		int count=0;
		for(int i=0;i<mList.size();i++)
		{
			Result mResult=mList.get(i);
			if(isBlack!=null)
			{
				if(!isBlack&&mResult.isBlack) continue;
				if(isBlack&&!mResult.isBlack) continue;
			}
			count++;
		}
		return count+"s";
	}
	public String getWinRatio(Boolean isBlack) //统计游戏胜率（不计入平局）
	{
		int winCount=0;
		int notDrawCount=0;
		for(int i=0;i<mList.size();i++)
		{
			Result mResult=mList.get(i);
			if(isBlack!=null)
			{
				if(!isBlack&&mResult.isBlack) continue;
				if(isBlack&&!mResult.isBlack) continue;
			}
			if(mResult.result==0) continue; //胜率不计平局
			if(mResult.result==1) winCount++;
			notDrawCount++;
		}
		if(notDrawCount==0) return "Null";
		float ratio=(winCount*100f)/notDrawCount;
		DecimalFormat df=new DecimalFormat("0.00");
		return df.format(ratio)+"%";
	}
}

class Result
{
	public int result; //游戏结果，1胜，0平，-1负
	public int pieceCount; //落子总数
	public boolean isBlack; //是否先攻

	public static String toString(Result result) //类转化字符串
	{
		if(result.isBlack) return result.result+" "+result.pieceCount+" t";
		else return result.result+" "+result.pieceCount+" f";
	}
	public static Result fromString(String string) //字符串转化类
	{
		List<String> strings=divideString(string);
		Result nResult=new Result();
		try{
			nResult.result=Integer.valueOf(strings.get(0));
			nResult.pieceCount=Integer.valueOf(strings.get(1));
			if(strings.get(2).equals("t")) nResult.isBlack=true;
			else nResult.isBlack=false;
		}catch(Exception e) {}
		return nResult;
	}
	private static List<String> divideString(String string) //字符串分割
	{
		List<String> strings=new ArrayList<>();
		int start=-1;
		for(int i=0;i<string.length();i++)
		{
			char c=string.charAt(i);
			if(c==' ')
			{
				if(start!=-1)
				{
					strings.add(string.substring(start, i));
					start=-1;
				}
			}
			else
			{
				if(start==-1) start=i;
			}
		}
		if(start!=-1) strings.add(string.substring(start, string.length()));
		return strings;
	}
}