/* 1.프로그램명 : CPU 스케줄링 시뮬레이터
 * 2.프로그램 실행환경 : window 10, 버전 2004, Eclipse IDE 2020-06, Intel Core i7, RAM 16GB, x64
 * 3.프로그램 개발환경 : Eclipse IDE 2020-06, JDK-14.0.2_window_x64
 * 4.프로그램 작성자 : 이해경
 * 5.프로그램 개요 : 7가지 CPU 스케줄링 시뮬레이터 개발 (FCFS, SJF, 비선점 Priority, 선점 Priority, RR, SRT, HRN)
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.lang.NumberFormatException;
import java.util.ArrayList;
import java.util.LinkedList;


public class MainScheduling extends JFrame{
	//====================프로세스 클래스====================//
	class process{
		int index;
		public String id;
		public int arrivedTime;
		public int serviceTime;
		public int priority;
		public int timeSliceSize;
		public int remainTime;
		int respondTime;
		int readyTime;
		int returnTime;
		public process(int pindex, String ID, int artime, int svtime, int prr, int ts) {
			index=pindex;
			id=ID;
			arrivedTime=artime;
			serviceTime=svtime;
			remainTime=svtime;
			priority=prr;
			timeSliceSize=ts;
			readyTime=0;
			respondTime=-1;
		}
		public void increaseReadyTime() {
			readyTime=readyTime+1;
		}
		public void checkRespondTime() {
			//모든 프로세스는 시작하고 3밀리초가 지나면 첫 반응이 나온다고 임의로 설정
			respondTime=readyTime+3;	
		}
		public void decreaseRemianTime() {
			remainTime=remainTime-1;
		}
		public void checkReturnTime(int runtime) {
			returnTime=runtime-arrivedTime;
		}
		//대기시간, 응답시간, 반환시간 초기화
		public void init() {
			remainTime=serviceTime;
			readyTime=0;
			respondTime=-1;
		}
	}
	
	//====================FCFS 클래스====================//
	class FCFS {
		public boolean isEnd = false;
		public int runtime=0;
		public ArrayList<Integer> timeLine = new ArrayList<Integer>();
		LinkedList<process> readyQueue = new LinkedList<>();
		process nowProcess = null;
		
		public void init() {
			runtime=0;
			readyQueue.clear();
			timeLine.clear();
			nowProcess=null;
			isEnd=false;
		}
		public void checkArrive(process checkProcess) {
			readyQueue.add(checkProcess);
		}
		public void checkReadyQueue() {
			if(nowProcess==null) {
				nowProcess=readyQueue.poll();
			}
			else if(nowProcess.remainTime<=0){
				nowProcess.checkReturnTime(runtime);
				nowProcess=readyQueue.poll();
			}
		}
		public void runProcess() {
			checkReadyQueue();
			if(nowProcess!=null) {
				if(nowProcess.respondTime<0) {
					nowProcess.checkRespondTime();
				}
				nowProcess.decreaseRemianTime();
				timeLine.add(nowProcess.index);
				runtime=runtime+1;
			}
			else {
				isEnd=true;
			}
		}
	}
	
	//====================SJF 클래스====================//
	class SJF extends FCFS{
		public void checkArrive(process checkProcess) {
			int inputindex=checkServiceTime(checkProcess);
			readyQueue.add(inputindex, checkProcess);
		}
		public int checkServiceTime(process checkProcess) {
			int i;
			for(i=0; i<readyQueue.size();i++) {
				if(checkProcess.serviceTime<readyQueue.get(i).serviceTime)
					return i;
			}
			return i;
		}
	}
	
	//====================HRN 클래스====================//
	class HRN extends FCFS{
		public void checkReadyQueue() {
			if(nowProcess==null) {
				nowProcess=readyQueue.poll();
			}
			else if(nowProcess.remainTime<=0){
				nowProcess.checkReturnTime(runtime);
				nowProcess=calcPriority();
			}
		}
		public process calcPriority() {
			int i;
			int minindex=0;
			process nextprocess = null;
			float nextPriority;
			float queuePriority;
			if(readyQueue.size()>0) {
				nextprocess=readyQueue.get(0);
				nextPriority=(float)(nextprocess.serviceTime+nextprocess.readyTime)/nextprocess.serviceTime;
				for(i=1; i<readyQueue.size();i++) {
					queuePriority=(float)(readyQueue.get(i).serviceTime+readyQueue.get(i).readyTime)/readyQueue.get(i).serviceTime;
					if(nextPriority<queuePriority)
						minindex=i;
				}
				nextprocess=readyQueue.get(minindex);
				readyQueue.remove(minindex);
			}
			return nextprocess;
		}
	}
	
	//====================비선점 Priority 클래스====================//
	class NonPreemptive extends FCFS{
		public void checkArrive(process checkProcess) {
			int inputindex=checkPriority(checkProcess);
			readyQueue.add(inputindex, checkProcess);
		}
		public int checkPriority(process checkProcess) {
			int i;
			for(i=0; i<readyQueue.size();i++) {
				if(checkProcess.priority<readyQueue.get(i).priority)
					return i;
			}
			return i;
		}
	}
	
	//====================선점 Priority 클래스====================//
	class Preemptive extends NonPreemptive{
		public void runProcess() {
			checkReadyQueue();
			if(nowProcess!=null) {
				if(readyQueue.size()>0) {
					if(nowProcess.priority>readyQueue.peek().priority)
						changeProcess();
				}
				if(nowProcess.respondTime<0) {
					nowProcess.checkRespondTime();
				}
				nowProcess.decreaseRemianTime();
				timeLine.add(nowProcess.index);
				runtime=runtime+1;
			}
			else {
				isEnd=true;
			}
		}
		public void changeProcess() {
			process tempProcess;
			tempProcess=nowProcess;
			nowProcess=readyQueue.poll();
			checkArrive(tempProcess);
		}
	}
	
	//====================RR 클래스====================//
	class RR extends FCFS{
		int timeslice = 0;
		public void checkReadyQueue() {
			if(nowProcess==null) {
				nowProcess=readyQueue.poll();
				timeslice=nowProcess.timeSliceSize;
			}
			else {
				if(nowProcess.remainTime<=0){
					nowProcess.checkReturnTime(runtime);
					nowProcess=readyQueue.poll();
					if(nowProcess!=null)
						timeslice=nowProcess.timeSliceSize;
				}
				else if(timeslice<=0) {
					if(readyQueue.size()>0)
						changeProcess();
					else
						timeslice=nowProcess.timeSliceSize;
				}
			}
		}
		public void runProcess() {
			checkReadyQueue();
			if(nowProcess!=null) {
				if(nowProcess.respondTime<0) {
					nowProcess.checkRespondTime();
				}
				nowProcess.decreaseRemianTime();
				timeLine.add(nowProcess.index);
				runtime=runtime+1;
				timeslice=timeslice-1;
			}
			else {
				isEnd=true;
			}
		}
		public void changeProcess() {
			if(nowProcess.remainTime>0) {
				readyQueue.add(nowProcess);
				nowProcess=readyQueue.poll();
				if(nowProcess!=null)
					timeslice=nowProcess.timeSliceSize;
			}
		}
	}
	
	//====================SRT 클래스====================//
	class SRT extends RR{
		public void checkArrive(process checkProcess) {
			int inputindex=checkRamainTime(checkProcess);
			readyQueue.add(inputindex, checkProcess);
		}
		public int checkRamainTime(process checkProcess) {
			int i;
			for(i=0; i<readyQueue.size();i++) {
				if(checkProcess.remainTime<readyQueue.get(i).remainTime)
					return i;
			}
			return i;
		}
		public void changeProcess() {	
			if(nowProcess.remainTime>0) {
				checkArrive(nowProcess);
				nowProcess=readyQueue.poll();
				if(nowProcess!=null)
					timeslice=nowProcess.timeSliceSize;
			}
		}
	}
	
	//====================변수 선언====================//
	public process[] processList = new process[6];
	public FCFS fcfs = new FCFS();
	public SJF sjf = new SJF();
	public HRN hrn = new HRN();
	public NonPreemptive nonp = new NonPreemptive();
	public Preemptive pree = new Preemptive();
	public RR rr = new RR();
	public SRT srt = new SRT();
	
	public ShowResult resultPanel = new ShowResult();	//결과화면
	
	String schedulingAltext = null;
	ArrayList<Integer> timeline = new ArrayList<Integer>();
	int runtime;
	
	//프로세스 입력
	JTextField[][] processInput = new JTextField[6][5];
	
	//====================UI 관련 코드====================//
	public MainScheduling() {
		setTitle("CPU 스케줄링 시뮬레이터");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(new UIpanel());
		setSize(700, 300);
		setVisible(true);
	}
	
	class UIpanel extends JPanel {
		public UIpanel() {
			setBackground(Color.WHITE);
			setLayout(null); // 패널의 배치관리자 제거
			
			//프로세스 타이틀
			JLabel[] processInputTitle = new JLabel[6];
			//프로세스 입력 목록
			JLabel processID = new JLabel("프로세스ID");
			processID.setHorizontalAlignment(JLabel.CENTER);
			processID.setSize(100, 20);
			processID.setLocation(10, 40);
			add(processID);
			JLabel processARTime = new JLabel("도착시간");
			processARTime.setHorizontalAlignment(JLabel.CENTER);
			processARTime.setSize(100, 20);
			processARTime.setLocation(10, 70);
			add(processARTime);
			JLabel processSVTime = new JLabel("서비스시간");
			processSVTime.setHorizontalAlignment(JLabel.CENTER);
			processSVTime.setSize(100, 20);
			processSVTime.setLocation(10, 100);
			add(processSVTime);
			JLabel processPRR = new JLabel("우선순위");
			processPRR.setHorizontalAlignment(JLabel.CENTER);
			processPRR.setSize(100, 20);
			processPRR.setLocation(10, 130);
			add(processPRR);
			JLabel processTS = new JLabel("시간할당량");
			processTS.setHorizontalAlignment(JLabel.CENTER);
			processTS.setSize(100, 20);
			processTS.setLocation(10, 160);
			add(processTS);
			
			//프로세스 초기화
			processList[0]=new process(0, "p0", 0, 35, 2, 20);
			processList[1]=new process(1, "p1", 10, 30, 0, 20);
			processList[2]=new process(2, "p2", 20, 45, 3, 20);
			processList[3]=new process(3, "p3", 5, 40, 1, 20);
			processList[4]=new process(4, "p4", 15, 10, 4, 20);
			processList[5]=new process(5, "p5", 25, 20, 5, 20);

			for(int i=0; i<processInputTitle.length;i++) {
				processInputTitle[i] = new JLabel("프로세스"+i);
				processInputTitle[i].setHorizontalAlignment(JLabel.CENTER);
				processInputTitle[i].setSize(80, 20);
				processInputTitle[i].setLocation(100+90*i, 10);
				add(processInputTitle[i]);
				for(int j=0; j<processInput[0].length; j++) {
					processInput[i][j] = new JTextField(10);
					processInput[i][j].setSize(80, 20);
					processInput[i][j].setLocation(110+90*i, 40+30*j);
					add(processInput[i][j]);
				}
				processInput[i][0].setText(""+processList[i].id);
				processInput[i][1].setText(""+processList[i].arrivedTime);
				processInput[i][2].setText(""+processList[i].serviceTime);
				processInput[i][3].setText(""+processList[i].priority);
				processInput[i][4].setText(""+processList[i].timeSliceSize);
			}
			
			//tf.setText("0");
			
			//시뮬레이션 실행 버튼
			JButton[] startsimulator = new JButton[7];
			startsimulator[0]=new JButton("FCFS");
			startsimulator[1]=new JButton("SJF");
			startsimulator[2]=new JButton("HRN");
			startsimulator[3]=new JButton("비선점우선");
			startsimulator[3].setMargin(new Insets(0, 0, 0, 0));
			startsimulator[4]=new JButton("선점우선");
			startsimulator[4].setMargin(new Insets(0, 0, 0, 0));
			startsimulator[5]=new JButton("RR");
			startsimulator[6]=new JButton("SRT");
			for(int i=0; i<startsimulator.length; i++) {
				startsimulator[i].setSize(80, 30);
				startsimulator[i].setLocation(20+90*i, 210);
				add(startsimulator[i]);
			}
			
			
			startsimulator[0].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeprocess();
					init_process();
					countRuntime();
					runFCFS();
					schedulingAltext="FCFS";
					timeline=fcfs.timeLine;
					resultPanel = new ShowResult();
					resultPanel.setVisible(true);
				}
				});
			startsimulator[1].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeprocess();
					init_process();
					countRuntime();
					runSJF();
					schedulingAltext="SJF";
					timeline=sjf.timeLine;
					resultPanel = new ShowResult();
					resultPanel.setVisible(true);
				}
				});
			startsimulator[2].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeprocess();
					init_process();
					countRuntime();
					runHRN();
					schedulingAltext="HRN";
					timeline=hrn.timeLine;
					resultPanel = new ShowResult();
					resultPanel.setVisible(true);
				}
				});
			startsimulator[3].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeprocess();
					init_process();
					countRuntime();
					runNonp();
					schedulingAltext="비선점우선";
					timeline=nonp.timeLine;
					resultPanel = new ShowResult();
					resultPanel.setVisible(true);
				}
				});
			startsimulator[4].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeprocess();
					init_process();
					countRuntime();
					runPree();
					schedulingAltext="선점우선";
					timeline=pree.timeLine;
					resultPanel = new ShowResult();
					resultPanel.setVisible(true);
				}
				});
			startsimulator[5].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeprocess();
					init_process();
					countRuntime();
					runRR();
					schedulingAltext="RR";
					timeline=rr.timeLine;
					resultPanel = new ShowResult();
					resultPanel.setVisible(true);
				}
				});
			startsimulator[6].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					changeprocess();
					init_process();
					countRuntime();
					runSRT();
					schedulingAltext="SRT";
					timeline=srt.timeLine;
					resultPanel = new ShowResult();
					resultPanel.setVisible(true);
				}
				});
		}
		public void init_process() {
			for(int i=0; i<processList.length; i++) {
				processList[i].init();
			}
		}
	}
	
	void changeprocess() {
		for(int i=0; i<processList.length; i++) {
			processList[i].id=processInput[i][0].getText();
			try {
				processList[i].arrivedTime=Integer.parseInt(processInput[i][1].getText());
				processList[i].serviceTime=Integer.parseInt(processInput[i][2].getText());
				processList[i].priority=Integer.parseInt(processInput[i][3].getText());
				processList[i].timeSliceSize=Integer.parseInt(processInput[i][4].getText());
				}
				catch(NumberFormatException ex) {	//정수가 아닌 문자열을 정수로 변환할 때의 예외 발생
					processInput[i][0].setText(""+processList[i].id);
					processInput[i][1].setText(""+processList[i].arrivedTime);
					processInput[i][2].setText(""+processList[i].serviceTime);
					processInput[i][3].setText(""+processList[i].priority);
					processInput[i][4].setText(""+processList[i].timeSliceSize);
					return;
				}
		}
	}
	
	void countRuntime() {
		runtime=0;
		for(int i=0; i<processList.length; i++) {
			runtime+=processList[i].serviceTime;
		}
	}
	
	//결과를 보여주는 패널
	class ShowResult extends JFrame{
		public ShowResult() {
			setTitle("스케줄링 결과");
			setBackground(Color.WHITE);
			setLayout(null); // 패널의 배치관리자 제거
			setSize(700, 300);
		}
		//간트 차트를 그리기 위한 코드
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			JLabel[] processIDtext = new JLabel[6];
			JLabel schedulingAl =new JLabel(schedulingAltext);
			schedulingAl.setHorizontalAlignment(JLabel.RIGHT);
			schedulingAl.setSize(80, 20);
			schedulingAl.setLocation(0, 40);
			add(schedulingAl);
			
			JLabel ganttTitle = new JLabel("간트 차트");
			ganttTitle.setHorizontalAlignment(JLabel.CENTER);
			ganttTitle.setSize(680, 20);
			ganttTitle.setLocation(0, 5);
			add(ganttTitle);
			
			//간트차트 시간 단위 표시
			JLabel timeText = new JLabel("0");
			timeText.setHorizontalAlignment(JLabel.LEFT);
			timeText.setSize(20, 20);
			timeText.setLocation(90, 60);
			add(timeText);
			
			//간트차트 그리기
			for(int i=0; i<runtime; i++) {
				int nowprocessIndex = timeline.get(i);
				if(nowprocessIndex==0) {
					g.setColor(Color.red);
				}
				else if(nowprocessIndex==1) {
					g.setColor(Color.orange);
				}
				else if(nowprocessIndex==2) {
					g.setColor(Color.green);
				}
				else if(nowprocessIndex==3) {
					g.setColor(Color.blue);
				}
				else if(nowprocessIndex==4) {
					g.setColor(Color.magenta);
				}
				else if(nowprocessIndex==5) {
					g.setColor(Color.cyan);
				}
				if(i==0) {
					processIDtext[nowprocessIndex]=new JLabel(processList[nowprocessIndex].id);
					processIDtext[nowprocessIndex].setHorizontalAlignment(JLabel.LEFT);
					processIDtext[nowprocessIndex].setSize(80, 20);
					processIDtext[nowprocessIndex].setLocation(93, 20);
					add(processIDtext[nowprocessIndex]);
				}
				//값이 다르면 새로운 프로세스 시작을 의미
				else if(nowprocessIndex!=timeline.get(i-1)) {
					processIDtext[nowprocessIndex]=new JLabel(processList[nowprocessIndex].id);
					processIDtext[nowprocessIndex].setHorizontalAlignment(JLabel.LEFT);
					processIDtext[nowprocessIndex].setSize(80, 20);
					processIDtext[nowprocessIndex].setLocation(93+3*i, 20);
					add(processIDtext[nowprocessIndex]);
					
					timeText = new JLabel(""+i);
					timeText.setHorizontalAlignment(JLabel.LEFT);
					timeText.setSize(40, 20);
					timeText.setLocation(90+3*i, 60);
					add(timeText);
				}
				else if(runtime==(i+1)) {
					timeText = new JLabel(""+(i+1));
					timeText.setHorizontalAlignment(JLabel.LEFT);
					timeText.setSize(40, 20);
					timeText.setLocation(90+3*i, 60);
					add(timeText);
				}
					g.fillRect(100+3*i, 70, 3, 20);
			}
			
			g.setColor(Color.darkGray);
			for(int i = 0; i<runtime; i++) {
				g.drawRect(100+3*i, 70, 3, 20);
			}
			
			JLabel readytime = new JLabel(">>대기시간(단위 : 밀리초)  "+ processList[0].id + " : " + processList[0].readyTime
					+" / " +  processList[1].id + " : " + processList[1].readyTime
					+" / " +  processList[2].id + " : " + processList[2].readyTime
					+" / " +  processList[3].id + " : " + processList[3].readyTime
					+" / " +  processList[4].id + " : " + processList[4].readyTime
					+" / " +  processList[5].id + " : " + processList[5].readyTime);
			readytime.setHorizontalAlignment(JLabel.CENTER);
			readytime.setSize(680, 20);
			readytime.setLocation(0, 95);
			add(readytime);
			
			float sumreadytime = 0;
			for(int i=0; i<processList.length;i++) {
				sumreadytime+=processList[i].readyTime;
			}
			JLabel readytimeAvg = new JLabel(">>평균대기시간(단위 : 밀리초)  "+sumreadytime/processList.length);
			readytimeAvg.setHorizontalAlignment(JLabel.CENTER);
			readytimeAvg.setSize(680, 20);
			readytimeAvg.setLocation(0, 120);
			add(readytimeAvg);
			
			JLabel respondtime = new JLabel(">>응답시간(단위 : 밀리초)  "+ processList[0].id + " : " + processList[0].respondTime
					+" / " +  processList[1].id + " : " + processList[1].respondTime
					+" / " +  processList[2].id + " : " + processList[2].respondTime
					+" / " +  processList[3].id + " : " + processList[3].respondTime
					+" / " +  processList[4].id + " : " + processList[4].respondTime
					+" / " +  processList[5].id + " : " + processList[5].respondTime);
			respondtime.setHorizontalAlignment(JLabel.CENTER);
			respondtime.setSize(680, 20);
			respondtime.setLocation(0, 150);
			add(respondtime);
			
			float sumRespontime = 0;
			for(int i=0; i<processList.length;i++) {
				sumRespontime+=processList[i].respondTime;
			}
			JLabel respondtimeAvg = new JLabel(">>평균응답시간(단위 : 밀리초)  "+sumRespontime/processList.length);
			respondtimeAvg.setHorizontalAlignment(JLabel.CENTER);
			respondtimeAvg.setSize(680, 20);
			respondtimeAvg.setLocation(0, 175);
			add(respondtimeAvg);
			
			JLabel returntime = new JLabel(">>반환시간(단위 : 밀리초)  "+ processList[0].id + " : " + processList[0].returnTime
					+" / " +  processList[1].id + " : " + processList[1].returnTime
					+" / " +  processList[2].id + " : " + processList[2].returnTime
					+" / " +  processList[3].id + " : " + processList[3].returnTime
					+" / " +  processList[4].id + " : " + processList[4].returnTime
					+" / " +  processList[5].id + " : " + processList[5].returnTime);
			returntime.setHorizontalAlignment(JLabel.CENTER);
			returntime.setSize(680, 20);
			returntime.setLocation(0, 205);
			add(returntime);
			
			float sumreturntime = 0;
			for(int i=0; i<processList.length;i++) {
				sumreturntime+=processList[i].returnTime;
			}
			JLabel returntimeAvg = new JLabel(">>평균반환시간(단위 : 밀리초)  "+sumreturntime/processList.length);
			returntimeAvg.setHorizontalAlignment(JLabel.CENTER);
			returntimeAvg.setSize(680, 20);
			returntimeAvg.setLocation(0, 230);
			add(returntimeAvg);
		}
	}
	//====================FCFS 실행====================//
	public void runFCFS() {
		fcfs.init();
		for(int i=0; i<=runtime; i++) {
			for(int j=0; j<processList.length;j++) {
				if(processList[j].arrivedTime==i)
					fcfs.checkArrive(processList[j]);
				else if(processList[j].respondTime<0&&processList[j].arrivedTime<i) {
					processList[j].increaseReadyTime();
				}
			}
			fcfs.runProcess();
				
			if(fcfs.isEnd)
				break;
		}
	}
	//====================SJF 실행====================//
	public void runSJF() {
		sjf.init();
		for(int i=0; i<=runtime; i++) {
			for(int j=0; j<processList.length;j++) {
				if(processList[j].arrivedTime==i)
					sjf.checkArrive(processList[j]);
				else if(processList[j].respondTime<0&&processList[j].arrivedTime<i) {
					processList[j].increaseReadyTime();
				}
			}
			sjf.runProcess();
			if(sjf.isEnd)
				break;
		}
	}
	//====================HRN 실행====================//
	public void runHRN() {
		hrn.init();
		for(int i=0; i<=runtime; i++) {
			for(int j=0; j<processList.length;j++) {
				if(processList[j].arrivedTime==i)
					hrn.checkArrive(processList[j]);
				else if(processList[j].respondTime<0&&processList[j].arrivedTime<i) {
					processList[j].increaseReadyTime();
				}
			}
			hrn.runProcess();
			if(hrn.isEnd)
				break;
		}
	}
	
	//====================비선점 Priority 실행====================//
	public void runNonp() {
		nonp.init();
		for(int i=0; i<=runtime; i++) {
			for(int j=0; j<processList.length;j++) {
				if(processList[j].arrivedTime==i)
					nonp.checkArrive(processList[j]);
				else if(processList[j].respondTime<0&&processList[j].arrivedTime<i) {
					processList[j].increaseReadyTime();
				}
			}
			nonp.runProcess();
			if(nonp.isEnd)
				break;
		}
	}
	
		//====================선점 Priority 실행====================//
		public void runPree() {
			pree.init();
			for(int i=0; i<=runtime; i++) {
				for(int j=0; j<processList.length;j++) {
					if(processList[j].arrivedTime==i)
						pree.checkArrive(processList[j]);
					else if(processList[j].respondTime<0&&processList[j].arrivedTime<i) {
						processList[j].increaseReadyTime();
					}
				}
				pree.runProcess();
				if(pree.isEnd)
					break;
			}
		}
		
		//====================RR 실행====================//
		public void runRR() {
			rr.init();
			for(int i=0; i<=runtime; i++) {
				for(int j=0; j<processList.length;j++) {
					if(processList[j].arrivedTime==i)
						rr.checkArrive(processList[j]);
					else if(processList[j].respondTime<0&&processList[j].arrivedTime<i) {
						processList[j].increaseReadyTime();
					}
				}
				rr.runProcess();
				if(rr.isEnd)
					break;
			}
		}
		
		//====================SRT 실행====================//
		public void runSRT() {
			srt.init();
			for(int i=0; i<=runtime; i++) {
				for(int j=0; j<processList.length;j++) {
					if(processList[j].arrivedTime==i)
						srt.checkArrive(processList[j]);
					else if(processList[j].respondTime<0&&processList[j].arrivedTime<i) {
						processList[j].increaseReadyTime();
					}
				}
				srt.runProcess();
				if(srt.isEnd)
					break;
			}
		}
		
	
	public static void main(String[] args) {
		new MainScheduling();
	}

}
