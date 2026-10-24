package com.example.clockcontroller;

import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final int bg=Color.rgb(11,16,32), panel=Color.rgb(22,30,52), muted=Color.rgb(151,166,194), mint=Color.rgb(97,231,208), white=Color.WHITE;
    private BluetoothAdapter adapter; private BluetoothSocket socket; private OutputStream output; private TextView connection, deviceLabel; private EditText commandInput; private LinearLayout root; private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private static final UUID SPP=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg); BluetoothManager bm=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE); adapter=bm==null?null:bm.getAdapter(); buildUi();}
    private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);if(stroke!=0)d.setStroke(1,stroke);return d;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private void pad(View v,int a){v.setPadding(a,a,a,a);}
    private void gap(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,h));}
    private TextView button(String label, boolean primary, Runnable action){TextView b=text(label,14,primary?bg:white,true);b.setGravity(Gravity.CENTER);b.setPadding(16,14,16,14);b.setBackground(shape(primary?mint:Color.rgb(34,44,70),16,primary?mint:Color.rgb(53,67,99)));b.setClickable(true);b.setFocusable(true);b.setOnClickListener(v->{action.run();});return b;}
    private void toast(String msg){Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();}
    private void buildUi(){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(bg);root=col();root.setPadding(22,18,22,28);sc.addView(root);setContentView(sc);
        LinearLayout top=row();TextView logo=text("◷",30,mint,true);logo.setGravity(Gravity.CENTER);logo.setBackground(shape(panel,18,0));top.addView(logo,new LinearLayout.LayoutParams(58,58));LinearLayout title=col();title.setPadding(14,0,0,0);title.addView(text("CLOCK CONTROLLER",19,white,true));title.addView(text("SMART DISPLAY · CONTROL PANEL",10,muted,true));top.addView(title,new LinearLayout.LayoutParams(0,-2,1));root.addView(top);gap(root,24);
        LinearLayout hero=col();hero.setPadding(20,22,20,22);hero.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(29,52,77),Color.rgb(20,29,51)}));((GradientDrawable)hero.getBackground()).setCornerRadius(24);hero.addView(text("YOUR CLOCK, CONNECTED",11,mint,true));gap(hero,12);hero.addView(text("Control your\ndigital clock.",30,white,true));gap(hero,8);hero.addView(text("Pair a device, connect, and send control commands.",14,muted,false));gap(hero,20);
        LinearLayout status=row();TextView dot=text("●",14,mint,true);status.addView(dot);connection=text("  Not connected",13,white,true);status.addView(connection);hero.addView(status);root.addView(hero);gap(root,22);
        root.addView(text("DEVICE CONNECTION",12,muted,true));gap(root,10);LinearLayout device=col();device.setPadding(16,16,16,16);device.setBackground(shape(panel,20,0));deviceLabel=text("No device selected",15,white,true);device.addView(deviceLabel);gap(device,5);device.addView(text("Classic Bluetooth · SPP",12,muted,false));gap(device,14);LinearLayout actions=row();TextView choose=button("Choose paired device",true,this::chooseDevice);actions.addView(choose,new LinearLayout.LayoutParams(0,-2,1));View sp=new View(this);actions.addView(sp,new LinearLayout.LayoutParams(8,1));TextView disconnect=button("Disconnect",false,()->disconnect(true));actions.addView(disconnect);device.addView(actions);root.addView(device);gap(root,22);
        root.addView(text("QUICK CONTROLS",12,muted,true));gap(root,10);root.addView(text("These example commands are editable in the source. Confirm your controller's command format before sending.",12,muted,false));gap(root,12);
        LinearLayout grid=row();LinearLayout left=col(),right=col();grid.addView(left,new LinearLayout.LayoutParams(0,-2,1));View sep=new View(this);grid.addView(sep,new LinearLayout.LayoutParams(10,1));grid.addView(right,new LinearLayout.LayoutParams(0,-2,1));left.addView(button("◉  Turn ON",true,()->send("ON")));gap(left,10);left.addView(button("☾  Night mode",false,()->send("NIGHT")));right.addView(button("○  Turn OFF",false,()->send("OFF")));gap(right,10);right.addView(button("◷  Show time",false,()->send("TIME")));root.addView(grid);gap(root,22);
        root.addView(text("CUSTOM COMMAND",12,muted,true));gap(root,10);LinearLayout sendCard=col();sendCard.setPadding(16,16,16,16);sendCard.setBackground(shape(panel,20,0));commandInput=new EditText(this);commandInput.setSingleLine(true);commandInput.setTextColor(white);commandInput.setHintTextColor(muted);commandInput.setHint("Type command… e.g. ON");commandInput.setTextSize(14);commandInput.setPadding(12,10,12,10);commandInput.setBackground(shape(Color.rgb(12,19,36),12,Color.rgb(53,67,99)));sendCard.addView(commandInput,new LinearLayout.LayoutParams(-1,-2));gap(sendCard,12);sendCard.addView(button("Send command  →",true,()->{String s=commandInput.getText().toString().trim();if(s.isEmpty()){toast("Enter a command first");return;}send(s);}));root.addView(sendCard);gap(root,18);
        TextView foot=text("SECURE CONNECTION  ·  LOCAL BLUETOOTH",10,muted,true);foot.setGravity(Gravity.CENTER);root.addView(foot);
    }
    private boolean permitted(){if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},101);toast("Allow Bluetooth permission to continue");return false;}return true;}
    private void chooseDevice(){if(!permitted())return;if(adapter==null){toast("Bluetooth is not available on this device");return;}if(!adapter.isEnabled()){toast("Turn on Bluetooth, then try again");return;}Set<BluetoothDevice> bonded;try{bonded=adapter.getBondedDevices();}catch(SecurityException e){toast("Bluetooth permission required");return;}if(bonded==null||bonded.isEmpty()){toast("No paired devices. Pair your clock in Android Settings first.");return;}ArrayList<BluetoothDevice> list=new ArrayList<>(bonded);String[] names=new String[list.size()];for(int i=0;i<list.size();i++){BluetoothDevice d=list.get(i);String n;try{n=d.getName();}catch(Exception e){n=null;}names[i]=(n==null?"Bluetooth device":n)+"\n"+d.getAddress();}new AlertDialog.Builder(this).setTitle("Choose paired device").setItems(names,(dlg,which)->connect(list.get(which))).setNegativeButton("Cancel",(d,w)->toast("Device selection cancelled")).show();toast("Choose your clock controller");}
    private void connect(BluetoothDevice device){if(!permitted())return;toast("Connecting to device…");connection.setText("  Connecting…");worker.execute(()->{BluetoothSocket s=null;try{s=device.createRfcommSocketToServiceRecord(SPP);s.connect();final BluetoothSocket connectedSocket=s;OutputStream out=connectedSocket.getOutputStream();runOnUiThread(()->{try{socket=connectedSocket;output=out;String n=device.getName();deviceLabel.setText((n==null?"Bluetooth device":n)+" · "+device.getAddress());connection.setText("  Connected");toast("Bluetooth connected OK");}catch(Exception e){toast("Connected, but UI update failed");}});}catch(Exception e){try{if(s!=null)s.close();}catch(Exception ignored){}runOnUiThread(()->{connection.setText("  Not connected");toast("Connection failed. Check pairing and controller power.");});}});}
    private void send(String command){if(output==null||socket==null||!socket.isConnected()){toast("Not connected — choose a paired device first");return;}final OutputStream out=output;worker.execute(()->{try{out.write((command+"\n").getBytes(StandardCharsets.UTF_8));out.flush();runOnUiThread(()->toast("Sent: "+command+" ✓"));}catch(Exception e){runOnUiThread(()->toast("Send failed: connection lost"));}});}
    private void disconnect(boolean showToast){try{if(socket!=null)socket.close();}catch(Exception ignored){}socket=null;output=null;if(connection!=null)connection.setText("  Not connected");if(showToast)toast("Disconnected");}
    @Override protected void onDestroy(){disconnect(false);worker.shutdownNow();super.onDestroy();}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==101){if(g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)toast("Bluetooth permission granted OK");else toast("Bluetooth permission denied");}}
}
