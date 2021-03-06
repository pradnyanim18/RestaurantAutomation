package login;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;


//Who you are logged in as- '0' = no one; 'h' = host; 'm' = manager; 'w'= waiter; 'c' = chef
 
import config.Configure;
public class LoginPage extends JPanel{

	public final String dataBaseAServerName = Configure.getDomainName("databaseacontroller");
	public final int dataBaseAPortNumber = Configure.getPortNumber("databaseacontroller");
	
	public boolean keypadLock;
	public char loggedIn;
	public String empName;
	public long currIDEntry;
	
	private JTextField textField;
	private JTextField txtPleaseEnterEmployee;
	private JTextField failedAttempt;
	
	public JFrame frame;
	
	LoginPage self;
	public LoginPage(JFrame frame){
		this.frame = frame;
		loggedIn='0';
		self = this;
		//Set color to blue
		setBackground(new Color(51, 255, 204));
		setLayout(null);
		updateScreen();
	}
	
	private void updateScreen() {
		removeAll();
		makeTime();
		makeHeaderText();
		makeIDTextField();
		makeKeypad();
		
	}

	private void logInToDBA(long empID){
		System.out.println("Saved name="+dataBaseAServerName);
		try(Socket client = new Socket(dataBaseAServerName, dataBaseAPortNumber)) {
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			DataInputStream in = new DataInputStream(client.getInputStream());
			String message = "L:"+empID;
			System.out.println(message);
			out.writeUTF(message);
			String ans =in.readUTF();
			loggedIn=ans.charAt(0);
			if(loggedIn!='0'&& loggedIn!='L'){
				empName=ans.substring(1);
			}
			in.close();
			out.close();
			client.close();
		} catch (Exception e){
			e.printStackTrace();
			loggedIn='0';
			return;
		}
	}
	
	private void makeIDTextField(){
		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		textField.setEditable(false);
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setBounds(550, 80, 100, 25);
		textField.setBackground(Color.LIGHT_GRAY);
		add(textField);
		textField.setColumns(10);
	}
	
	private void makeKeypad(){
		
		failedAttempt = new JTextField("Employee ID not recognized.");
		failedAttempt.setFont(new Font("Tahoma", Font.PLAIN, 12));
		failedAttempt.setEditable(false);
		failedAttempt.setHorizontalAlignment(SwingConstants.CENTER);
		failedAttempt.setBounds(450, 520, 300, 25);
		failedAttempt.setForeground(Color.RED);
		add(failedAttempt);
		failedAttempt.setVisible(false);
		
		
		JButton logInButton = new JButton("Log In");
		logInButton.setForeground(Color.WHITE);
		logInButton.setBackground(new Color(0, 128, 0));
		logInButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				failedAttempt.setVisible(false);
				while(keypadLock){}
				keypadLock=true;
				logInToDBA(currIDEntry);
				textField.setText("");
				if(loggedIn=='0'){
					currIDEntry=-1;
					failedAttempt.setText("Employee ID not recognized.");
					failedAttempt.setVisible(true);
				}
				else if(loggedIn=='L'){
					currIDEntry=-1;
					failedAttempt.setText("Employee already logged in.");
					failedAttempt.setVisible(true);
				}
				else{
					TabApp.login(self);
				}
				keypadLock=false;
			}
		});
		logInButton.setBounds(725, 150, 90, 30);
		add(logInButton);
		
		JButton deleteButton = new JButton("Delete");
		deleteButton.setForeground(Color.WHITE);
		deleteButton.setBackground(new Color(255, 0, 0));
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				failedAttempt.setVisible(false);
				while(keypadLock){}
				keypadLock=true;
				textField.setText("");
				currIDEntry=-1;
				keypadLock=false;
			}
		});
		deleteButton.setBounds(725, 270, 90, 30);
		add(deleteButton);
		
		JButton[] keypad = new JButton[10];
		int xRow1 = 500;
		int row=0;
		int cnt =0;
		for(int i = 1; i<10; i++){
			if(cnt>2){
				cnt=0;
				row++;
			}
			int d=i;
			keypad[i] = new JButton(""+i);
			keypad[i].setBounds(xRow1+ 75*cnt, 150+row*60, 50, 30);
			keypad[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					while(keypadLock){}
					keypadLock=true;
					textField.setText(textField.getText()+d);
					if(currIDEntry==-1){
						currIDEntry = 0;
					}
					currIDEntry=currIDEntry*10+d;
					keypadLock=false;
				}
			});
			add(keypad[i]);
			cnt++;
		}
			
		keypad[0] = new JButton("0");
		keypad[0].setBounds(575, 150+180, 50, 30);
		keypad[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				while(keypadLock){}
				keypadLock=true;
				textField.setText(textField.getText()+"0");
				if(currIDEntry==-1){
					currIDEntry=0;
				}
				currIDEntry=currIDEntry*10;
				keypadLock=false;
			}
		});
		add(keypad[0]);
	}
	
	private void makeHeaderText(){
		txtPleaseEnterEmployee = new JTextField("Please Enter Employee ID:");
		txtPleaseEnterEmployee.setEditable(false);
		txtPleaseEnterEmployee.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtPleaseEnterEmployee.setHorizontalAlignment(SwingConstants.CENTER);
		txtPleaseEnterEmployee.setBounds(500, 20, 200, 30);
		add(txtPleaseEnterEmployee);

	}

	
	public void logOut(long empID) {
		//set up socket (as client)
		try(Socket client = new Socket(dataBaseAServerName, dataBaseAPortNumber)) {
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			String message = "O:"+empID;
			out.writeUTF(message);
			out.close();
			client.close();
			
		} catch (Exception e) {
			loggedIn='0';
			return;
		}
	}
	
		private void makeTime(){
			Calendar cal=Calendar.getInstance();
			JTextField timeHeader;
			String tmp=""+cal.getTime();
			tmp=tmp.substring(0, tmp.length()-12);
			timeHeader=new JTextField(tmp);
			timeHeader.setEditable(false);
			timeHeader.setFont(new Font("Tahoma",Font.PLAIN,14));
			timeHeader.setHorizontalAlignment(SwingConstants.CENTER);
			timeHeader.setBounds(0, 0, 300, 30);
			add(timeHeader);
		}
	
}
