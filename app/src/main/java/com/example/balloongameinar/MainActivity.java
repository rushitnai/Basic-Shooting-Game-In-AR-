package com.example.balloongameinar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.widget.Button;
import android.widget.TextView;

import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Scene scene;
    private Camera camera;
    private  ModelRenderable bulletRenderable;
    private Button button;
    TextView tv_timer,tv_balloonsLeft;
    private  int balloonsLeft=10;
    private  boolean shouldstartTimer = true;
    private Point point;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display = getWindowManager().getDefaultDisplay();
        point= new Point();
        display.getRealSize(point);

        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.btn_shoot);
        tv_timer = findViewById(R.id.timer);
        tv_balloonsLeft = findViewById(R.id.tv_bl);

        CustomArFragment arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        scene = arFragment.getArSceneView().getScene();
        camera = scene.getCamera();
        addBallons();
        buildBulletModel();
        button.setOnClickListener(view -> {

            if(shouldstartTimer){
                startTimer();
                shouldstartTimer= false;
            }
            shoot();


        });

    }

    private void shoot() {

        Ray ray = camera.screenPointToRay(point.x/2f,point.y/2f);
        Node node = new Node();
        node.setRenderable(bulletRenderable);
        scene.addChild(node);
        new Thread(() -> {
            for(int i = 0;i<200;i++){
                int finalI = i;
                runOnUiThread(() -> {
                    

                    Vector3 vector3 = ray.getPoint(finalI * 0.1f);
                    node.setWorldPosition(vector3);
                    Node nodeIncontact = scene.overlapTest(node);
                    if(nodeIncontact != null){
                        balloonsLeft--;
                        tv_balloonsLeft.setText("Objects Left :" + balloonsLeft);
                        scene.removeChild(nodeIncontact);

                    }
                });
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> {
                scene.removeChild(node);
            });


        }).start();
    }

    private void startTimer() {
        new Thread(() -> {
            int seconds = 0;
            while (balloonsLeft>0){

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                seconds++;
                int minutePassed = seconds/60;
                int secPassed = seconds % 60;
                runOnUiThread(() ->tv_timer.setText(minutePassed +":" + secPassed ) );
            }


        }).start();
    }

    //shows the bullet path
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void buildBulletModel() {
        Texture.builder().setSource(this,R.drawable.texture)
                .build()
                .thenAccept(texture -> {


                    MaterialFactory.makeOpaqueWithTexture(this,texture)
                            .thenAccept(material -> {

                                bulletRenderable = ShapeFactory.makeSphere(0.01f,new Vector3(0f,0f,0f),
                                        material);

                            });
                });
    }

    //add object to scene view
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addBallons() {
        ModelRenderable
                .builder().setSource(this, R.raw.arcticfox_posed).build()
                .thenAccept(renderable -> {

                    for(int i =0 ; i<10 ;i++){

                        Node node = new Node();
                        node.setRenderable(renderable);
                        scene.addChild(node);
                        Random random = new Random();
                        int x = random.nextInt(15);
                        int y = random.nextInt(15);
                        int z = random.nextInt(10);
                        z= -z;
                        node.setWorldPosition(new Vector3(
                                (float) x,
                                y/10f,
                                (float)z
                        ));

                    }


                });
    }
}