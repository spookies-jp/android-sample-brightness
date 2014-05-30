package jp.co.spookies.android.brightness;

import java.util.Locale;

import jp.co.spookies.android.brightness.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.widget.TextView;

public class BrightnessActivity extends Activity implements
		SensorEventListener, TextToSpeech.OnInitListener {
	private SensorManager sensorManager;
	private TextToSpeech tts;
	private TextView textView;
	private boolean isBright;
	private final static float BRIGHT_THRESHOLD = 500.0f; // 明るさ判定の閾値
	private final static int REQUEST_CODE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// TextToSpeech用の音声データがインストールされているかのチェック
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, REQUEST_CODE);

		textView = (TextView) findViewById(R.id.tv_message);
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		isBright = true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		// 明るさセンサーの登録
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		if (sensor != null) {
			sensorManager.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_UI);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		// センサーの解除
		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// TextToSpeechの終了
		if (tts != null) {
			tts.shutdown();
		}
	}

	@Override
	protected void
			onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE) {
			// TextToSpeechのデータが存在するかどうか
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// TextToSpeechの初期化
				tts = new TextToSpeech(this, this);
			} else {
				// TextToSpeechのデータをインストールするアクティビティを起動
				Intent intent = new Intent(
						TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(intent);
				finish();
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (tts == null) {
			return;
		}
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		if (!isBright && event.values[0] > BRIGHT_THRESHOLD) {
			lp.screenBrightness = 1.0f; // 画面を明るく
			tts.setPitch(1.2f); // 声を高く
			tts.speak("it's bright.", TextToSpeech.QUEUE_ADD, null); // 話す
			textView.setText("BRIGHT"); // 画面にメッセージ
			textView.setTextColor(Color.WHITE); // メッセージの色を変更
			isBright = true;
		} else if (isBright && event.values[0] <= BRIGHT_THRESHOLD) {
			lp.screenBrightness = 0.2f; // 画面を暗く
			tts.setPitch(0.8f); // 声を低く
			tts.speak("here is very DARK.", TextToSpeech.QUEUE_ADD, null); // 話す
			textView.setText("DARK"); // 画面にメッセージ
			textView.setTextColor(Color.BLACK); // メッセージの色を変更
			isBright = false;
		}

		// 画面の明るさ変更を反映
		getWindow().setAttributes(lp);
	}

	@Override
	public void onInit(int status) {
		tts.setLanguage(Locale.US);
		tts.setSpeechRate(0.7f);
	}
}