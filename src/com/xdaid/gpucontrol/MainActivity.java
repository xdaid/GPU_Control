package com.xdaid.gpucontrol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends FragmentActivity
{
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;

	static private String FILE_GPU = "/sys/kernel/gpu_control/gpu_control_active";
	static private String FILE_MAX = "/sys/kernel/gpu_control/max_freq";
	static private String FILE_CMD = "/system/bootmenu/2nd-boot/cmdline";

	static private boolean meetRequirements = true;
	static private boolean controlEnabled = true;
	static private boolean zcacheEnabled = true;

	static private String[] values = new String[] { "n/a", "16", "33", "66", "100", "133", "200", "266" };
 	static private String[] rawValues = new String[] { "n/a", "16666666", "33333333", "66666666", "100000000", "133333333", "200000000", "266666666" };



 	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_main, menu);
		menu.getItem(0).setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem arg0)
			{
				AlertDialog.Builder dbuilder = new AlertDialog.Builder(MainActivity.this);
				dbuilder.setTitle(R.string.app_name);
				dbuilder.setMessage(R.string.msg_about);
				dbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface arg0, int arg1) { }
				});
				dbuilder.show();
				return false;
			}
		});
		menu.getItem(1).setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem arg0)
			{
				finish();
				return false;
			}
		});
		return true;
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			switch (position)
			{
			case 0: return new Fragment_INF();
			case 1: return new Fragment_GPU();
			case 2: return new Fragment_CMD();
			}
			return null;
		}

		@Override
		public int getCount()
		{
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			switch (position)
			{
			case 0: return getString(R.string.fragment_inf_title);
			case 1: return getString(R.string.fragment_gpu_title);
			case 2: return getString(R.string.fragment_cmd_title);
			}
			return null;
		}
	}


	public static class Fragment_INF extends Fragment
	{
		public Fragment_INF()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			View layout = inflater.inflate(R.layout.fragment_inf, null);
			TextView tv1 = (TextView)layout.findViewById(R.id.fragment_inf_tv1);
			tv1.setGravity(Gravity.CENTER);
			tv1.setText(R.string.fragment_inf_label1);
			TextView tv2 = (TextView)layout.findViewById(R.id.fragment_inf_tv2);
			tv2.setGravity(Gravity.CENTER);
			tv2.setText(R.string.fragment_inf_label2);
			return layout;
		}
	}


	public static class Fragment_GPU extends Fragment
	{
		public Fragment_GPU()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			String fileGPU = execsh("cat " + FILE_GPU);
			String fileMAX = execsh("cat " + FILE_MAX);
			controlEnabled = fileGPU.length() == 0 ? false : Integer.parseInt(fileGPU) == 1;
			int textColor = controlEnabled ? 0xFF000000 : 0xFFA0A0A0;

			View layout = inflater.inflate(R.layout.fragment_gpu, null);

			Switch sv1 = (Switch)layout.findViewById(R.id.fragment_gpu_sv1);
			sv1.setText(R.string.fragment_gpu_switch);
			sv1.setChecked(controlEnabled);

			final TextView tv1 = (TextView)layout.findViewById(R.id.fragment_gpu_tv1);
			tv1.setText(R.string.fragment_gpu_select);
			tv1.setTextColor(textColor);

			final ValuePicker vp1 = (ValuePicker)layout.findViewById(R.id.fragment_gpu_vp1);
			vp1.setValues(values);
            vp1.setTextColor(textColor);
        	vp1.setOnValueChangedListener(new ValuePicker.OnValueChangedListener()
			{
		    	@Override
		    	public void valueChanged(ValuePicker sender, int value) { }
			});

			final TextView bt1 = (TextView)layout.findViewById(R.id.fragment_gpu_bt3);
			bt1.setText("apply");
			bt1.setTextColor(textColor);

			sv1.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton v, boolean checked)
				{
					if (!meetRequirements)
					{
						controlEnabled = false;
						v.setChecked(false);
						return;
					}
					controlEnabled = checked;
					int textColor = checked ? 0xFF000000 : 0xFFA0A0A0;
					tv1.setTextColor(textColor);
					vp1.setTextColor(textColor);
					bt1.setTextColor(textColor);
					String permGPU = getFilePerm(FILE_GPU);
			    	String numPerm = numericPermissionString(permGPU);
					String[] cmds = new String[]
					{
						"chmod 777 " + FILE_GPU,
						"echo " + (controlEnabled ? "1" : "0") + " > " + FILE_GPU,
						"chmod " + numPerm + " " + FILE_GPU
					};
			    	execsu(cmds);
				}
			});
			if (fileMAX.length() == 0)
			{
				meetRequirements = false;
				textColor = 0xFFA0A0A0;
				tv1.setTextColor(textColor);
				vp1.setTextColor(textColor);
				bt1.setTextColor(textColor);
	            vp1.setValue("n/a");
				Toast.makeText(getActivity(), R.string.msg_nomaxfreq, Toast.LENGTH_LONG).show();
				return layout;
			}

			int clk = Integer.parseInt(fileMAX) / 1000000;
			vp1.setValue(String.valueOf(clk));

			bt1.setOnTouchListener(new OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					if (!controlEnabled)
					{
						return true;
					}
					if (event.getAction() == MotionEvent.ACTION_DOWN)
					{
						v.setBackgroundColor(0x8000B0F0);
						return true;
					}
					if (event.getAction() == MotionEvent.ACTION_MOVE)
					{
						Rect bounds = new Rect();
						v.getDrawingRect(bounds);
						if (!bounds.contains((int)event.getX(), (int)event.getY()))
						{
							v.setBackgroundResource(R.drawable.shape_round_rect);
						}
						return true;
					}
					if (event.getAction() == MotionEvent.ACTION_UP)
					{
						if (v == bt1) v.setBackgroundResource(R.drawable.shape_round_rect);
						Rect bounds = new Rect();
						v.getDrawingRect(bounds);
						if (bounds.contains((int)event.getX(), (int)event.getY()))
						{
							if (!controlEnabled) return true;
							String rawValue = getRawValue( vp1.getValue() );
							if (rawValue.equals("n/a")) return true;
					    	
							String permMAX = getFilePerm(FILE_MAX);
							String numPerm = numericPermissionString(permMAX);
							String[] cmds = new String[]
							{
								"chmod 777 " + FILE_MAX,
								"echo " + rawValue + " > " + FILE_MAX,
								"chmod " + numPerm + " " + FILE_MAX
							};
					    	execsu(cmds);
							Toast.makeText(getActivity(), R.string.msg_okmaxfreq, Toast.LENGTH_LONG).show();
						}
						return true;	
					}
					v.setBackgroundResource(R.drawable.shape_round_rect);
					return true;
				}
			});
			return layout;
		}
	}


	public static class Fragment_CMD extends Fragment
	{
		public Fragment_CMD()
		{
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
		{
			final String fileCMD = execsh("cat " + FILE_CMD);
			zcacheEnabled = fileCMD.length() == 0 ? false : fileCMD.contains("zcache");
			View layout = inflater.inflate(R.layout.fragment_cmd, null);
			Switch sv1 = (Switch)layout.findViewById(R.id.fragment_cmd_sv1);
			sv1.setText(R.string.fragment_cmd_switch);
			sv1.setChecked(zcacheEnabled);
			sv1.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton v, boolean checked)
				{
					if (!meetRequirements)
					{
						zcacheEnabled = false;
						v.setChecked(false);
						return;
					}
					zcacheEnabled = checked;
					if ( checked &&  fileCMD.contains("zcache")) return;
					if (!checked && !fileCMD.contains("zcache")) return;
					String fileContent = fileCMD;
					if (checked)
					{
						fileContent = fileContent.trim();
						fileContent = fileContent + " zcache";
					}
					else
					{
						fileContent = fileContent.replace("zcache", "");
						fileContent = fileContent.trim();
					}
					String permCMD = getFilePerm(FILE_CMD);
			    	String numPerm = numericPermissionString(permCMD);
					String[] cmds = new String[]
					{
						"mount -o remount,rw -t ext3 /dev/block/mmcblk1p21 /system",
						"chmod 777 " + FILE_CMD,
						"echo " + "\"" + fileContent + "\"" + " > " + FILE_CMD,
						"chmod " + numPerm + " " + FILE_CMD
					};
			    	execsu(cmds);
				}
			});
			return layout;
		}
	}


	public static String execsu(String[] commands)
    {
    	String output = "";
    	try
    	{
    		Process process = Runtime.getRuntime().exec("su");
    		DataOutputStream os = new DataOutputStream(process.getOutputStream());
    		for (String cmd : commands) { os.writeBytes(cmd + "\n"); os.flush(); }
    		os.writeBytes("exit\n");
    		os.flush();
    		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		StringBuffer sb = new StringBuffer();
    		char[] buffer = new char[4096];
    		for (int read; (read = br.read(buffer)) > 0; sb.append(buffer, 0, read));
    		br.close();
    		process.waitFor();
    		output = sb.toString().trim();
    	}
    	catch (Exception e) { }
    	return output;
    }


	public static String execsu(String command)
    {
    	String output = "";
    	String cmd = command.endsWith("\n") ? command : command + "\n";
    	try
    	{
    		Process process = Runtime.getRuntime().exec("su");
    		DataOutputStream os = new DataOutputStream(process.getOutputStream());
    		os.writeBytes(cmd + "\n");
    		os.flush();
    		os.writeBytes("exit\n");
    		os.flush();
    		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		StringBuffer sb = new StringBuffer();
        	char[] buffer = new char[4096];
        	for (int read; (read = br.read(buffer)) > 0; sb.append(buffer, 0, read));
        	br.close();
        	process.waitFor();
        	output = sb.toString().trim();
    	}
    	catch (Exception e) { }
    	return output;
    }


	public static String execsh(String command)
    {
    	String output = "";
    	String cmd = command.endsWith("\n") ? command : command + "\n";
    	try
    	{
    		Process process = Runtime.getRuntime().exec(cmd);
    		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		StringBuffer sb = new StringBuffer();
    		char[] buffer = new char[4096];
    		for (int read; (read = br.read(buffer)) > 0; sb.append(buffer, 0, read));
    		br.close();
    		process.waitFor();
    		output = sb.toString().trim();
    	}
    	catch (Exception e) { }
    	return output;
    }


    public static String getRawValue(String value)
    {
    	ArrayList<String> al = new ArrayList<String>();
    	for (String s : values) al.add(s);
    	return rawValues[al.indexOf(value)];
    }


    public static String getFilePerm(String file)
    {
    	String res = execsh("ls -a -l -d " + file);
    	if (res.length() > 10) return res.substring(1, 10);
    	res = execsh("sh ls -a -l -d " + file);
    	if (res.length() > 10) return res.substring(1, 10);
    	res = execsh("busybox ls -a -l -d " + file);
    	if (res.length() > 10) return res.substring(1, 10);
    	return "rwxrwxrwx";
    }


    public static String numericPermissionString(String permissionString)
    {
    	String[] fields = new String[]{ permissionString.substring(0, 3), permissionString.substring(3, 6), permissionString.substring(6) };
    	String numeric = "";
    	for (String field : fields) 
        {
    		if (field.equals("rwx")) numeric += "7"; else
    		if (field.equals("rw-")) numeric += "6"; else
        	if (field.equals("r-x")) numeric += "5"; else
            if (field.equals("r--")) numeric += "4"; else
            if (field.equals("-wx")) numeric += "3"; else
         	if (field.equals("-w-")) numeric += "2"; else
            if (field.equals("--x")) numeric += "1"; else
            if (field.equals("---")) numeric += "0";
        }
    	return numeric;
    }
}
