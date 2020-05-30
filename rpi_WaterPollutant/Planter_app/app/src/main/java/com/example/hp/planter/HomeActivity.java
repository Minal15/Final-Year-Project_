package com.example.hp.planter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.contrib.driver.adc.ads1xxx.Ads1xxx;
import com.google.firebase.FirebaseApp;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 * <p>
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class HomeActivity extends Activity {

    private static final String Pin_motor = "BCM17";
    private static final String Pin_moisture_sensor = "BCM19";
    private static final String Pin_tem_sensor = "BCM19";

    private Gpio Gpio_motor;
    private Gpio Gpio_moisture_sensor;
    private Gpio Gpio_tem_sensor;
    public DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    public DatabaseReference motor = mDatabase.child("motor");
    //    private static final String I2C_BUS = "0x48";
//    private static final int I2C_ADDRESS = 0x48;
//    private static final Ads1015.Gain GAIN = Ads1015.Gain.TWO_THIRDS;
//    private Ads1015 ads1015;
//    private Ads1015.Channel channel;
    private Handler mainHandler;
    private Handler backgroundHandler;
    private Ads1xxx mAdcDriver;
    private Handler mHandler;
    private HandlerThread mReadThread;
    private Handler mHandlerTem;
    Handler mHandlerSalinity;
    private HandlerThread mReadThreadTem;
    private HandlerThread mReadThreadSalinity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        PeripheralManager service = PeripheralManager.getInstance();
        try {
            Gpio_moisture_sensor = service.openGpio(Pin_moisture_sensor);
            Gpio_moisture_sensor.setDirection(Gpio.DIRECTION_IN);
            Gpio_moisture_sensor.setEdgeTriggerType(Gpio.EDGE_BOTH);
            Gpio_moisture_sensor.registerGpioCallback(mSetOutterApplianceCallBack);

            Gpio_motor = service.openGpio(Pin_motor);
            Gpio_motor.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            Gpio_motor.setValue(true);

            Gpio_tem_sensor = service.openGpio(Pin_tem_sensor);
            Gpio_tem_sensor.setDirection(Gpio.DIRECTION_IN);
           /* List<String> deviceList = service.getI2cBusList();
            Firebase.setPh_sensor(deviceList);

            if (deviceList.isEmpty()) {
                Log.i(TAG, "No I2C bus available on this device.");
            } else {
                Log.i(TAG, "List of available devices: " + deviceList);
            }

            mDevice = service.openI2cDevice(I2C_DEVICE_NAME, I2C_ADDRESS);
            setRegisterFlag(mDevice,I2C_ADDRESS);*/

        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

        try {
            Log.d(TAG, "Initializing ADC Driver");
            mAdcDriver = new Ads1xxx("I2C1", Ads1xxx.Configuration.ADS1115);

            // Increase default range to fit +3.3V
            mAdcDriver.setInputRange(Ads1xxx.RANGE_6_144V);

            // Set up I/O polling thread
            mReadThread = new HandlerThread("ADC Reader");
            mReadThreadTem = new HandlerThread("ADC tem Reader");
            mReadThreadSalinity = new HandlerThread("ADC Salinity Reader");
            mReadThread.start();
            mReadThreadTem.start();
            mReadThreadSalinity.start();
            mHandler = new Handler(mReadThread.getLooper());
            mHandlerTem = new Handler(mReadThreadTem.getLooper());
            mHandlerSalinity = new Handler(mReadThreadSalinity.getLooper());
            mHandler.post(mReadAction);
            mHandlerTem.post(mReadActionTem);
            mHandlerSalinity.post(mReadActionSalinity);
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize ADC driver", e);
        }

        Firebase.ph_sensor.setValue(7);

    }

    private static final int DELAY_MS = 1000; // 10 samples/second
    /* Read a single analog sample and log the result */
    private Runnable mReadAction = new Runnable() {
        @Override
        public void run() {
            try {
                // Read differential between IN0+/IN1-
                final int value = mAdcDriver.readDifferentialInput(Ads1xxx.INPUT_DIFF_0P_1N);
//                Firebase.ph_sensor.setValue(value);
                Log.i(TAG, "Current ADC value: " + value);
            } catch (IOException e) {
                Log.e(TAG, "Unable to read analog sample", e);
            }

            mHandler.postDelayed(this, DELAY_MS);
        }
    };

    /* Read a single analog sample and log the Temperature result */
    private Runnable mReadActionTem = new Runnable() {
        @Override
        public void run() {
            try {
                // Read differential between IN0+/IN1-
                final long value = mAdcDriver.readDifferentialInput(Ads1xxx.INPUT_DIFF_1P_3N);
                if (value <40){
                    Firebase.setmTemperature(value);
                }
//                Firebase.setmTemperature(value);
                Log.i(TAG, "Current ADC value: " + value);
            } catch (IOException e) {
                Log.e(TAG, "Unable to read analog sample", e);
            }

            mHandlerTem.postDelayed(this, 2000);
        }
    };


    private Runnable mReadActionSalinity = new Runnable() {
        @Override
        public void run() {
            try {
                // Read differential between IN0+/IN1-
                final long value = mAdcDriver.readDifferentialInput(Ads1xxx.INPUT_DIFF_0P_3N);
//                Firebase.setSalinity(value);
                Log.i(TAG, "Current ADC value: " + value);
            } catch (IOException e) {
                Log.e(TAG, "Unable to read analog sample", e);
            }
            mHandlerSalinity.postDelayed(this, 3000);
        }
    };


    private final Runnable readAnLoop = new Runnable() {
        @Override
        public void run() {

            double[] ph_val = new double[10];
            for (int i = 0; i < 10; i++) {
                double result = 0;
                // final double result = ads1015.read(channel);
                int ADS1015_REG_CONFIG_MUX_SINGLE_0 = 0x4;  // Single-ended AIN0

//                try {
//                    // Get the voltage difference between IN0+ and IN1-
//                    //result = mAdcDriver.readSingleEndedVoltage(ADS1015_REG_CONFIG_MUX_SINGLE_0 );
//                    result = mAdcDriver.readDifferentialVoltage(Ads1xxx.INPUT_DIFF_0P_1N);
//
//                } catch (IOException e) {
//                    // error reading result
//                    Log.e(TAG, "error reading result ", e);
//
//                }
//                ph_val[i] = result;
            }
            // final float result = ads1015.read(channel);
            double temp;
            for (int i = 0; i < 9; i++) {
                for (int j = i + 1; j < 10; j++) {
                    if (ph_val[i] > ph_val[j]) {
                        temp = ph_val[i];
                        ph_val[i] = ph_val[j];
                        ph_val[j] = temp;
                    }
                }
            }
            double ph_avg = 0.0;
            for (int i = 2; i < 8; i++) {
                ph_avg += ph_val[i];
            }
            ph_avg = ph_avg / 6;

            double phv = (ph_avg * 5.0) / 1024;
            final double ph_value = (5.34 * phv) - 15.2673828;//y=mx+b
            mainHandler.post(new Runnable() {
                @Override
                public void run() {

                    Firebase.setph_status(ph_value);
//                   Firebase.ph_lower_limit();
//                   Firebase.ph_upper_limit();
//                    double upper_limit = Firebase.ph_upper_limit;
//                    double lower_limit = Firebase.ph_lower_limit;
//                    Firebase.setPh_sensor(ph_value);
//                    if(ph_value < lower_limit ||  ph_value > upper_limit){
//
//                        Firebase.setph_status(0);
//                    }else {
//
//                        Firebase.setph_status(1);
//                    }

                }
            });
            backgroundHandler.postDelayed(readAnLoop, TimeUnit.SECONDS.toMillis(5));
        }
    };


    private GpioCallback mSetOutterApplianceCallBack = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            if (gpio == null) {
                return true;
            }
            setGpio_moisture_sensor(gpio);
            return true;
        }
    };

    private void setGpio_moisture_sensor(Gpio gpio) {

        try {
            if (gpio.getValue()) {
//                Firebase.setmTemperature(true);
            } else {
//                Firebase.setmTemperature(false);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error on setting the value of moisture sensor at line 86 in main activity", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        motor.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer mmotor = dataSnapshot.getValue(Integer.class);
                try {
                    change_motor_state(mmotor, Gpio_motor);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

   /* public void setRegisterFlag(I2cDevice device, int address) throws IOException {
        // Read one register from slave
        byte value = device.readRegByte(address);
        // Set bit 6
        value |= 0x48;
        Firebase.setPh_sensor(value);
        // Write the updated value back to slave
        device.writeRegByte(address, value);
    }*/

    public void change_motor_state(Integer state, Gpio motor) throws IOException {
        if (motor != null) {
            if (state == 1) {
                motor.setValue(true);
            } else {
                motor.setValue(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Gpio_moisture_sensor != null) {
            Gpio_moisture_sensor.unregisterGpioCallback(mSetOutterApplianceCallBack);
            try {
                Gpio_moisture_sensor.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        //ads1015.close();
        // Close the block when finished:
        mReadThread.quit();

        try {
            mAdcDriver.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close ADC driver", e);
        } finally {
            mAdcDriver = null;
        }

    }
}
