package appzone.com.recipientapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mukesh.permissions.AppPermissions;

public class SignIn extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;
    private int RC_SIGN_IN = 3131;

    // runtime permesstion

    private AppPermissions mRuntimePermission;

    private static final int ALL_REQUEST_CODE = 0;
    private static final String[] ALL_PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
// basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

// Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    public void login(View v) {
        mRuntimePermission = new AppPermissions(this);

        if (mRuntimePermission.hasPermission(ALL_PERMISSIONS)) {
            // Location permission granted
        } else {
            mRuntimePermission.requestPermission(ALL_PERMISSIONS, ALL_REQUEST_CODE);
        }

        if (!mRuntimePermission.hasPermission(ALL_PERMISSIONS))
            return;
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        //   GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                // Get account information
                String mFullName = acct.getDisplayName();
                String mEmail = acct.getEmail();

                
                Toast.makeText(this, mFullName + "\n" + mEmail, Toast.LENGTH_SHORT).show();

                startActivity(new Intent (SignIn.this,MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Can Not Get Data", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show();
    }
}
