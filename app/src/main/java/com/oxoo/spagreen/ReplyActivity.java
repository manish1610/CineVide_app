package com.oxoo.spagreen;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.oxoo.spagreen.adapters.ReplyAdapter;
import com.oxoo.spagreen.models.CommentsModel;
import com.oxoo.spagreen.utils.ApiResources;
import com.oxoo.spagreen.utils.ToastMsg;
import com.oxoo.spagreen.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReplyActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etComment;
    private Button btnComment;

    private List<CommentsModel> list=new ArrayList<>();
    private ReplyAdapter replyAdapter;
    private String strCommentID,strAllReplyURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Reply");

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "reply_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        btnComment=findViewById(R.id.btn_comment);
        etComment=findViewById(R.id.et_comment);
        recyclerView=findViewById(R.id.recyclerView);

        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            etComment.setBackground(getResources().getDrawable(R.drawable.rounded_black_transparent));
            btnComment.setTextColor(getResources().getColor(R.color.grey_20));
        }


        replyAdapter=new ReplyAdapter(this,list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(replyAdapter);


        strCommentID=getIntent().getStringExtra("id");

        strAllReplyURL=new ApiResources().getGetAllReply()+"&&id="+strCommentID;

        final SharedPreferences preferences=getSharedPreferences("user",MODE_PRIVATE);
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etComment.getText().toString().equals("")){

                    new ToastMsg(ReplyActivity.this).toastIconError(getString(R.string.comment_empty));

                }else {

                    String commentUrl = new ApiResources().getAddReplyURL()
                            .concat("&&comments_id=")
                            .concat(strCommentID)
                            .concat("&&user_id=")
                            .concat(preferences.getString("id","0"))
                            .concat("&&comment=").concat(etComment.getText().toString());

                    commentUrl = commentUrl.replaceAll(" ", "%20");
                    commentUrl = commentUrl.replaceAll("\n", "%0A");
                    addComment(commentUrl);
                }
            }
        });

        getComments(strAllReplyURL);


    }


    private void addComment(String url){


        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    if (response.getString("status").equals("success")){

                        recyclerView.removeAllViews();
                        list.clear();
                        getComments(strAllReplyURL);
                        etComment.setText("");

                    }else {
                        new ToastMsg(ReplyActivity.this).toastIconError(response.getString("message"));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                new ToastMsg(ReplyActivity.this).toastIconError("can't comment now ! try later");
            }
        });

        VolleySingleton.getInstance(ReplyActivity.this).addToRequestQueue(jsonObjectRequest);

    }

    private void getComments(String url){

        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for (int i =0;i<response.length();i++){

                    try {

                        JSONObject jsonObject=response.getJSONObject(i);

                        CommentsModel model=new CommentsModel();

                        model.setName(jsonObject.getString("user_name"));
                        model.setImage(jsonObject.getString("user_img_url"));
                        model.setComment(jsonObject.getString("comments"));

                        list.add(model);

                        replyAdapter.notifyDataSetChanged();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        VolleySingleton.getInstance(ReplyActivity.this).addToRequestQueue(jsonArrayRequest);



    }

}
