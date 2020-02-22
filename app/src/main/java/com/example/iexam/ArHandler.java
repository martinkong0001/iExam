package com.example.iexam;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class ArHandler implements Scene.OnUpdateListener {

    private AppCompatActivity main;
    private ArFragment fragment;
    private ViewRenderable snellenChart;
    private Anchor anchor;
    private AnchorNode anchorNode;

    public ArHandler(AppCompatActivity main) {
        this.main = main;
        fragment = (ArFragment)main.getSupportFragmentManager().
                findFragmentById(R.id.fragment);
        buildSnellenChart();

    }

    //Build the Snellen chart as a ViewRenderable
    private void buildSnellenChart() {
        ViewRenderable.builder()
                .setView(fragment.getContext(), R.layout.snellen_imgboard)
                .build()
                .thenAccept(renderable -> snellenChart = renderable);
    }

    private void placeSnellenChart() {
        clearAnchor();

        //Place the Snellen chart 3m in front of camera
        Frame frame = fragment.getArSceneView().getArFrame();
        Session session = fragment.getArSceneView().getSession();
        anchor = session.createAnchor(frame.getCamera().getPose()
                .compose(Pose.makeTranslation(0, 0, -3f)).extractTranslation());
        anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(fragment.getArSceneView().getScene());

        Node node = new Node();
        node.setParent(anchorNode);
        node.setRenderable(snellenChart);
        fragment.getArSceneView().getScene().addOnUpdateListener(this);

        //Rotate the chart so that it faces towards camera
        Vector3 cameraPosition = fragment.getArSceneView().getScene().
                getCamera().getWorldPosition();
        Vector3 snellenPosition = node.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, snellenPosition);
        Quaternion rotation = Quaternion.lookRotation(direction, Vector3.up());
        node.setWorldRotation(rotation);

        //Scale the chart so that it is 1.6m in height
        Box box = (Box)snellenChart.getCollisionShape();
        Vector3 renderableSizeMeter = box.getSize();
        float renderableHeightMeter = renderableSizeMeter.y;
        float scale = 1.6f / renderableHeightMeter;
        node.setWorldScale(new Vector3(scale, scale, scale));
    }

    //Remove Snellen chart from the scene
    private void clearAnchor() {
        anchor = null;
        if (anchorNode != null) {
            fragment.getArSceneView().getScene().removeChild(anchorNode);
            anchorNode.getAnchor().detach();
            anchorNode.setParent(null);
            anchorNode = null;
        }
        fragment.getArSceneView().getScene().removeOnUpdateListener(this);
    }

    //Is the user still 3m away from Snellen chart?
    public void onUpdate(FrameTime frameTime) {

        if (anchorNode != null) {
            Frame frame = fragment.getArSceneView().getArFrame();
            Pose objectPose = anchor.getPose();
            Pose cameraPose = frame.getCamera().getPose();
            float dx = objectPose.tx() - cameraPose.tx();
            float dy = objectPose.ty() - cameraPose.ty();
            float dz = objectPose.tz() - cameraPose.tz();

            ///Compute the straight-line distance from camera
            float distanceMeter = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (Math.abs(distanceMeter - 3f) > 0.5f) {
                Toast.makeText(main.getApplicationContext(),
                        "You are no longer in the correct position. Please try again.",
                        Toast.LENGTH_LONG).show();
                clearAnchor();
            }
        }
    }

    public ArSceneView getArScene() {
        return fragment.getArSceneView();
    }

    public void addSnellenChart() {
        placeSnellenChart();
    }

    public void deleteSnellenChart() {
        clearAnchor();
    }

    public boolean isChartLive() {
        return anchor != null;
    }

}