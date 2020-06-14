package com.matkigae.mpproject.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.SkuDetails;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.material.dialog.MaterialDialogs;
import com.matkigae.mpproject.R;
import com.matkigae.mpproject.data.MatchingInfo;
import com.matkigae.mpproject.data.PetcareInfo;
import com.matkigae.mpproject.data.UserInfo;
import com.matkigae.mpproject.listeners.NavigationViewItemListener;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;

public class PaymentActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    FirebaseDatabase mDb = FirebaseDatabase.getInstance();
    NavigationView mNv;
    DrawerLayout mDl;
    Toolbar mTb;
    Button mBtnDoPayment;
    Button mBtnCancelPayment;
    PetcareInfo mPetcareInfo;
    String mStartTime;
    String mEndTime;

    private BillingProcessor bp;
    public static ArrayList<SkuDetails> products;
    private MaterialDialogs purchaseDialog;


    private void initView(){
        mTb = findViewById(R.id.toolbar);
        setSupportActionBar(mTb);
        //SetToolbar

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //햄버거 메뉴 활성화
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_hamburger_menu);
        //햄버거 메뉴 아이콘 설정

        mNv = findViewById(R.id.nav_view);
        mDl = findViewById(R.id.drawer_layout);
        mBtnDoPayment = findViewById(R.id.button_payment_confirmPayment);
        mBtnCancelPayment = findViewById(R.id.button_payment_cancelPayment);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        initView();
        mNv.setNavigationItemSelectedListener(new NavigationViewItemListener(this));

        Intent intent = getIntent();
        mPetcareInfo = intent.getParcelableExtra("petcareinfo");
        final String mStartTime = intent.getStringExtra("startdate");
        final String mEndTime = intent.getStringExtra("enddate");

        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArZIEcCAx7aRVH0Cz9d95oz+96cKc/Op/Yg87KZnhSAz3uajnfr3XJ9r5a9RzGBImRPt/18HIyn1N8zussnijARvj8CfgPCVriOI0LP8sPJYnD6+sSnnUrzKYMdsGDy4YUEOETfn05TXPT68nqF05aXInEYjMu9NPKFI5tI7zyqmKUNgsgnY38y2SIwrEQqfysaZhEuOXdn+lUB/9ZTSYP+yoLi45yUwgZ2XAsfoOJQjtYnWBnNs3gpba5f2yg0X4OvceN26DPlhWEM57LSmqCXFIYL78cOhbb3mVSBF/8SpAgxSTH2VyMH6RkgdPVS0oe8smyumybu9DmaPO3SeYAQIDAQAB", this);
        bp.initialize();

        mBtnDoPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (bp.isPurchased("payment_1")) {
//                    bp.consumePurchase("payment_1");
//                }
//                bp.purchase(PaymentActivity.this, "payment_1");

                MatchingInfo matchingInfo = new MatchingInfo(PaymentActivity.this.mPetcareInfo,mStartTime,mEndTime,FirebaseAuth.getInstance().getUid());;
                DatabaseReference ref = mDb.getReference().child("matchinginfo");
                ref.push().setValue(matchingInfo);
            }

        });

        mBtnCancelPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentActivity.this, PetCareActivity.class);
                startActivity(intent);
            }
        });


        mPetcareInfo = getIntent().getParcelableExtra("petcareinfo");

    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        switch (id){
            case android.R.id.home: // 좌측의 햄버거 메뉴 버튼을 눌렀을 때
                mDl.openDrawer(mNv); //Drawer의 NavigationView가 튀어나오도록 해준다.
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        // * 구매 완료시 호출
        // productId: 구매한 sku (ex) no_ads)
        // details: 결제 관련 정보
        SkuDetails sku = bp.getPurchaseListingDetails(productId);
        // 하트 100개 구매에 성공하였습니다! 메세지 띄우기
        String purchaseMessage = sku.title + "결제가 완료되었습니다!\n결제확인 페이지로 넘어갑니다.";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("예약 알림").setMessage(purchaseMessage);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(PaymentActivity.this, PaidActivity.class);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public void onPurchaseHistoryRestored() {
// * 구매 정보가 복원되었을때 호출
        // bp.loadOwnedPurchasesFromGoogle() 하면 호출 가능
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        // * 구매 오류시 호출
        // errorCode == Constants.BILLING_RESPONSE_RESULT_USER_CANCELED 일때는
        // 사용자가 단순히 구매 창을 닫은것임으로 이것 제외하고 핸들링하기.
        if (errorCode != Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
            String errorMessage = "결제 실패" + " (" + errorCode + ")";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBillingInitialized() {
        // * 처음에 초기화됬을때.
    }
}
