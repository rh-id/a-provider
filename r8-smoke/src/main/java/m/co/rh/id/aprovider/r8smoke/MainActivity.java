package m.co.rh.id.aprovider.r8smoke;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Launch host for the harness: {@link SmokeApplication#onCreate()} starts the
 * smoke suite, this activity just shows a hint while it runs.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("a-provider R8 smoke harness running - outcomes land in SmokeResult");
        setContentView(textView);
    }
}
