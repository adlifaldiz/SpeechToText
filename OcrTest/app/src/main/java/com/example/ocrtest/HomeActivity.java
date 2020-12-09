package com.example.ocrtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ocrtest.Adapter.IdiomAdapter;
import com.example.ocrtest.Model.IdiomModel;
import com.example.ocrtest.Response.IdiomResponse;
import com.example.ocrtest.Rest.ApiClient;
import com.example.ocrtest.Rest.RestApi;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import in.myinnos.alphabetsindexfastscrollrecycler.IndexFastScrollRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements IdiomAdapter.OnSuggestionClickListener  {

    private ImageView previewImage;
    private TextView textButton;
    private EditText resultEt;
    private CardView imageCard, scanButton;
    private Button clearImage;
    private Uri uri;
    private Bitmap bitmap;
    private IndexFastScrollRecyclerView idiomRecylcer;
    private RestApi mApiInterface;
    private RecyclerView.LayoutManager mLayoutManager;
    private ViewGroup containerMain;
    private SwipeRefreshLayout refreshLayout;
    private TextToSpeech textToSpeech;
    private ImageView speechButton;

    private List<IdiomModel> idiomModelList = new ArrayList<>();
    private IdiomAdapter idiomAdapter;

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        containerMain = findViewById(R.id.container_main);
        previewImage = findViewById(R.id.imageView);
        resultEt = findViewById(R.id.result_text);
        textButton = findViewById(R.id.text_button);
        imageCard = findViewById(R.id.image_card);
        clearImage = findViewById(R.id.clear_image);
        scanButton = findViewById(R.id.scan_button);
        refreshLayout = findViewById(R.id.refresh);
        speechButton = findViewById(R.id.speech_button);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        changeButtonText();

        clearImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransitionManager.beginDelayedTransition(containerMain);
                previewImage.setImageDrawable(null);
                changeButtonText();
            }
        });



        idiomRecylcer = findViewById(R.id.recyler_idiom);
        idiomRecylcer.setIndexBarCornerRadius(10);
        idiomRecylcer.setIndexBarTransparentValue((float) 1);
        idiomRecylcer.setIndexbarWidth(40);
        idiomRecylcer.setPreviewPadding(10);
        idiomRecylcer.setIndexbarHighLightTextColor(R.color.colorPrimaryDark);
        idiomRecylcer.setIndexBarColor(R.color.colorPrimaryDark);
        idiomRecylcer.setIndexBarStrokeVisibility(false);
        idiomRecylcer.setPreviewColor(R.color.colorPrimaryDark);
        mLayoutManager = new LinearLayoutManager(this);
        idiomRecylcer.setLayoutManager(mLayoutManager);
        mApiInterface = ApiClient.getClient().create(RestApi.class);
        refresh();




        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                CropImage.startPickImageActivity(HomeActivity.this);
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                    checkPermission();
                }
                speechDialog(resultEt);
            }
        });



        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                refresh();
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    textToSpeech.setLanguage(new Locale("su","SU"));
                }
            }
        });

        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textInput = resultEt.getText().toString();

                textToSpeech.speak(textInput,TextToSpeech.QUEUE_FLUSH,null);
            }
        });
    }

    private void changeButtonText() {
        TransitionManager.beginDelayedTransition(containerMain);
        Drawable drawable = previewImage.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            textButton.setText("Rekam Lagi");
            clearImage.setVisibility(View.VISIBLE);
            imageCard.setVisibility(View.VISIBLE);
        } else {
            textButton.setText("Rekam");
            clearImage.setVisibility(View.GONE);
            imageCard.setVisibility(View.GONE);
        }
    }

    private void refresh() {
        final Dialog chooseDialog;
        LayoutInflater chooseInflater;
        View chooseDialogView;

        chooseDialog = new Dialog(HomeActivity.this, R.style.Theme_AppCompat_Light_NoActionBar);
        chooseInflater = getLayoutInflater();
        chooseDialogView = chooseInflater.inflate(R.layout.dialog_loading, null);
        chooseDialog.setContentView(chooseDialogView);
        chooseDialog.setCancelable(true);
        chooseDialog.setCanceledOnTouchOutside(true);
        chooseDialog.show();
        Window window = chooseDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Call<IdiomResponse> pesananRespondCall = mApiInterface.getIdiom();
        pesananRespondCall.enqueue(new Callback<IdiomResponse>() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onResponse(Call<IdiomResponse> call, Response<IdiomResponse> response) {
                chooseDialog.dismiss();
                IdiomResponse idiomResponse = response.body();
                assert response.body() != null;
                if (idiomResponse.isStatus()){
                    final List<IdiomModel> idiomModelList = response.body().getIdiomModelList();

                    Log.d("Retrofit Get", "Jumlah data pesanan: " + idiomResponse.getMessage());
                    idiomAdapter = new IdiomAdapter(idiomModelList, HomeActivity.this::onSuggestionClick);
                    idiomRecylcer.setAdapter(idiomAdapter);
                    idiomAdapter.notifyDataSetChanged();


                    //fungsi search
                    resultEt.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                        @Override
                        public void afterTextChanged(Editable editable) {
//                            TransitionManager.beginDelayedTransition(containerMain);
                            if (resultEt.getText().toString().length() > 0) {
                                resultEt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0);
                                speechButton.setVisibility(View.VISIBLE);
                            } else {
                                resultEt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                                speechButton.setVisibility(View.GONE);
                            }
                            filter(editable.toString());
                        }
//
                        private void filter(String text) {
//                            TransitionManager.beginDelayedTransition(containerMain);
                            final ArrayList<IdiomModel> filteredList = new ArrayList<>();
                            for (IdiomModel item : idiomModelList) {
                                if (item.getPribasa().toLowerCase().contains(text.toLowerCase()) || item.getEjaan().toLowerCase().contains(text.toLowerCase())) {
                                    filteredList.add(item);
                                }
                            }
                            idiomAdapter.filterList(filteredList);
                        }
                    });
//
                    resultEt.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
//                            TransitionManager.beginDelayedTransition(containerMain);
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                if (event.getRawX() >= resultEt.getRight() - resultEt.getTotalPaddingRight()) {
                                    // your action for drawable click event
                                    resultEt.setText("");
                                    return true;
                                }
                            }
                            return false;
                        }
                    });


                } else {
                    Log.d("Retrofit Get", "Jumlah data pesanan: " +
                            String.valueOf(idiomModelList.size()));
                }

            }
            @Override
            public void onFailure(Call<IdiomResponse> call, Throwable t) {
                Log.e("Retrofit Get", t.toString());
                chooseDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode== Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this,data);
            if (CropImage.isReadExternalStoragePermissionsRequired(this,imageUri)){
                uri = imageUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
                }
            } else {
                startCrop(imageUri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                 previewImage.setImageURI(result.getUri());
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this,"Gambar berhasil diambil", Toast.LENGTH_SHORT).show();
                getTextFromImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        changeButtonText();
    }

    private void getTextFromImage() {
        //Bitmap bitmap= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.id.imageView);
        TextRecognizer textRecognizer=new TextRecognizer.Builder(getApplicationContext()).build();
        if(!textRecognizer.isOperational()){
            Toast.makeText(getApplicationContext(),"Could Not Detect Text",Toast.LENGTH_SHORT);
        }
        else{
            Frame frame=new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> sparseArray=textRecognizer.detect(frame);
            StringBuilder stringBuilder=new StringBuilder();
            for(int i=0;i<sparseArray.size();i++)
            {
                TextBlock textBlock=sparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");
            }
            String textResult = stringBuilder.toString().replaceAll("\\s+$", "");;
            resultEt.setText(textResult);
        }
    }

    private void startCrop(Uri imageUri) {
        CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true).start(this);
    }

    @Override
    public void onSuggestionClick(int position) {
        Call<IdiomResponse> pesananRespondCall = mApiInterface.getIdiom();
        pesananRespondCall.enqueue(new Callback<IdiomResponse>() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onResponse(Call<IdiomResponse> call, Response<IdiomResponse> response) {
                IdiomResponse idiomResponse = response.body();
                assert response.body() != null;
                if (idiomResponse.isStatus()){
                    final List<IdiomModel> idiomModelList = response.body().getIdiomModelList();
                    resultEt.setText(idiomModelList.get(position).getEjaan());
                } else {
                    Log.d("Retrofit Get", "Jumlah data pesanan: " +
                            String.valueOf(idiomModelList.size()));
                }

            }
            @Override
            public void onFailure(Call<IdiomResponse> call, Throwable t) {
                Log.e("Retrofit Get", t.toString());
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void speechDialog(EditText resultEt) {
        final Dialog speechDialog;
        LayoutInflater speechInflater;
        View speechDialogView;

        speechDialog = new Dialog(HomeActivity.this, R.style.DialogSlideAnim);
        speechInflater = getLayoutInflater();
        speechDialogView = speechInflater.inflate(R.layout.dialog_speech, null);
        speechDialog.setContentView(speechDialogView);
        speechDialog.setCancelable(true);
        speechDialog.setCanceledOnTouchOutside(true);
        speechDialog.show();
        Window window = speechDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);

        EditText speechResult;
        TextView rekamLagiButton;
        Button okButton;
        LottieAnimationView recordAnimation;
        speechResult = speechDialogView.findViewById(R.id.speech_result);
        rekamLagiButton = speechDialogView.findViewById(R.id.rekam_lagi_button);
        okButton = speechDialogView.findViewById(R.id.oke_button);
        recordAnimation = speechDialogView.findViewById(R.id.record_animation);


        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "su-SU");
        speechRecognizerIntent.putExtra(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS, "su-SU");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "su-SU");
        speechRecognizer.startListening(speechRecognizerIntent);


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                speechResult.setText("");
                speechResult.setHint("Listening...");
                okButton.setVisibility(View.GONE);
                recordAnimation.playAnimation();
                recordAnimation.loop(true);
            }

            @Override
            public void onRmsChanged(float v) {
                Log.d("speech", "onReadyForSpeech: "+ v);
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
                Log.d("speech", "onReadyForSpeech: "+ bytes);
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
                speechResult.setHint("Coba Lagi...");
                okButton.setVisibility(View.GONE);
                rekamLagiButton.setText("Rekam Lagi");
                recordAnimation.pauseAnimation();
            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                speechResult.setText(data.get(0));
                rekamLagiButton.setText("Rekam Lagi");
                okButton.setVisibility(View.VISIBLE);
                recordAnimation.pauseAnimation();
            }

            @Override
            public void onPartialResults(Bundle bundle) {
                Log.d("speech", "onReadyForSpeech: "+ bundle);
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
                Log.d("speech", "onReadyForSpeech: "+ i + bundle);
            }
        });

        rekamLagiButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    rekamLagiButton.setText("Rekam Lagi");
                    recordAnimation.pauseAnimation();
                    speechRecognizer.stopListening();
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    speechRecognizer.startListening(speechRecognizerIntent);
                    rekamLagiButton.setText("Berhenti");
                }
                return false;
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = speechResult.getText().toString();
                resultEt.setText(result);
                speechDialog.dismiss();
            }
        });

    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }
}