package com.example.chatgpttest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton,musicButton;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    View screenView;
    List<String> theWholeChat;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        welcomeTextView = findViewById(R.id.welcome_text);
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_btn);
        musicButton = findViewById(R.id.music_btn);
        screenView = findViewById(R.id.relative_layout);
        screenView.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.erzaback ));
        //setup of recycler view
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);//to scroll from bottom to top
        recyclerView.setLayoutManager(llm);
        initialize();
        theWholeChat = new ArrayList<>();
        sendButton.setOnClickListener((v)-> {
            String question = messageEditText.getText().toString().trim();
            theWholeChat.add(question);
            //Toast.makeText(this,question,Toast.LENGTH_LONG).show();
            addToChat(question,Message.SENT_BY_ME);
            messageEditText.setText("");
            callAPI(question);
            welcomeTextView.setVisibility(View.GONE);
        });
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MediaPlayer music = MediaPlayer.create(MainActivity.this, R.raw.music);
                if(music.isPlaying()){
                    music.pause();
                }
                else {
                    music.start();
                }
            }
        });

    }
    void addToChat(String message,String sentBy){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }
    void addResponse(String response){
        theWholeChat.add(response);
        messageList.remove(messageList.size()-1);
        addToChat(response,Message.SENT_BY_BOT);
    }
    void callAPI(String question) {
        //okhttp setup
        //first add implementation of okhttp to build.gradle(app)
        messageList.add(new Message("Erza is typing...",Message.SENT_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","gpt-3.5-turbo");
            JSONArray messageArr = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("role","user");
            obj.put("content",theWholeChat);
            messageArr.put(obj);
            jsonBody.put("messages",messageArr);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                //.header("Authorization","Bearer <Type your API Key here>")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failure to load response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    // See completions on OpenAI documentation
                    //choices is where the message is stored in the response json file
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    addResponse("Failure to load response due to "+response.body().toString());
                }
            }
        });
    }
    void initialize(){
        String q = "You are Erza, a helpful assistant." ;
        JSONObject jsonBod = new JSONObject();
        try {
            jsonBod.put("model","gpt-3.5-turbo");
            JSONArray messageAr = new JSONArray();
            JSONObject ob = new JSONObject();
            ob.put("role","user");
            ob.put("content",q);
            messageAr.put(ob);
            jsonBod.put("messages",messageAr);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        RequestBody bod = RequestBody.create(jsonBod.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization","Bearer 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")
                .post(bod)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //addResponse("Failure to load response due to "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }
}
