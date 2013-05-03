package com.vitco.engine;

import com.threed.jpct.World;

/**
 * Synchronized version of the world.
 */
public class SyncWorld extends World {

    @Override
    public synchronized void dispose() {
        super.dispose();
    }

    @Override
    public synchronized void unlockMatrices() {
        super.unlockMatrices();
    }

    @Override
    public synchronized com.threed.jpct.Lights getLights() {
        return super.getLights();
    }

    @Override
    public synchronized int getSize() {
        return super.getSize();
    }

    @Override
    public synchronized void setMainObjectID(int i) {
        super.setMainObjectID(i);
    }

    @Override
    public synchronized int getMainObjectID() {
        return super.getMainObjectID();
    }

    @Override
    public synchronized com.threed.jpct.Object3D getMainObject() {
        return super.getMainObject();
    }

    @Override
    public synchronized com.threed.jpct.Camera getCamera() {
        return super.getCamera();
    }

    @Override
    public synchronized com.threed.jpct.Portals getPortals() {
        return super.getPortals();
    }

    @Override
    public synchronized com.threed.jpct.VisList getVisibilityList() {
        return super.getVisibilityList();
    }

    @Override
    public synchronized void decoupleVisibilityList() {
        super.decoupleVisibilityList();
    }

    @Override
    public synchronized void newCamera() {
        super.newCamera();
    }

    @Override
    public synchronized void setCameraTo(com.threed.jpct.Camera camera) {
        super.setCameraTo(camera);
    }

    @Override
    public synchronized void invertCulling(boolean b) {
        super.invertCulling(b);
    }

    @Override
    public synchronized int getCameraSector() {
        return super.getCameraSector();
    }

    @Override
    public synchronized void removeObject(int i) {
        super.removeObject(i);
    }

    @Override
    public synchronized void removeObject(com.threed.jpct.Object3D object3D) {
        super.removeObject(object3D);
    }

    @Override
    public synchronized com.threed.jpct.Object3D getObject(int i) {
        return super.getObject(i);
    }

    @Override
    public synchronized com.threed.jpct.Object3D getObjectByName(java.lang.String s) {
        return super.getObjectByName(s);
    }

    @Override
    public synchronized int addObject(com.threed.jpct.Object3D object3D) {
        return super.addObject(object3D);
    }

    @Override
    public synchronized void addObjects(com.threed.jpct.Object3D[] object3Ds) {
        super.addObjects(object3Ds);
    }

    @Override
    public synchronized int addLight(com.threed.jpct.SimpleVector simpleVector, float v, float v1, float v2) {
        return super.addLight(simpleVector, v, v1, v2);
    }

    @Override
    public synchronized int addLight(com.threed.jpct.SimpleVector simpleVector, java.awt.Color color) {
        return super.addLight(simpleVector, color);
    }

    @Override
    public synchronized void removeLight(int i) {
        super.removeLight(i);
    }

    @Override
    public synchronized void setLightRotation(int i, com.threed.jpct.SimpleVector simpleVector, float v, float v1, float v2) {
        super.setLightRotation(i, simpleVector, v, v1, v2);
    }

    @Override
    public synchronized void setLightRotation(int i, float v, float v1, float v2) {
        super.setLightRotation(i, v, v1, v2);
    }

    @Override
    public synchronized void setLightPosition(int i, com.threed.jpct.SimpleVector simpleVector) {
        super.setLightPosition(i, simpleVector);
    }

    @Override
    public synchronized void setLightVisibility(int i, boolean b) {
        super.setLightVisibility(i, b);
    }

    @Override
    public synchronized void setLightDiscardDistance(int i, float v) {
        super.setLightDiscardDistance(i, v);
    }

    @Override
    public synchronized void setLightDistanceOverride(int i, float v) {
        super.setLightDistanceOverride(i, v);
    }

    @Override
    public synchronized float getLightDistanceOverride(int i) {
        return super.getLightDistanceOverride(i);
    }

    @Override
    public synchronized void setLightAttenuation(int i, float v) {
        super.setLightAttenuation(i, v);
    }

    @Override
    public synchronized void setLightIntensity(int i, float v, float v1, float v2) {
        super.setLightIntensity(i, v, v1, v2);
    }

    @Override
    public synchronized float getLightAttenuation(int i) {
        return super.getLightAttenuation(i);
    }

    @Override
    public synchronized float getLightDiscardDistance(int i) {
        return super.getLightDiscardDistance(i);
    }

    @Override
    public synchronized com.threed.jpct.SimpleVector getLightPosition(int i) {
        return super.getLightPosition(i);
    }

    @Override
    public synchronized com.threed.jpct.SimpleVector getLightIntensity(int i) {
        return super.getLightIntensity(i);
    }

    @Override
    public synchronized void setWorldProcessor(com.threed.jpct.WorldProcessor worldProcessor) {
        super.setWorldProcessor(worldProcessor);
    }

    @Override
    public synchronized void setAmbientLight(int i, int i1, int i2) {
        super.setAmbientLight(i, i1, i2);
    }

    @Override
    public synchronized int[] getAmbientLight() {
        return super.getAmbientLight();
    }

    @Override
    public synchronized void setFogging(int i) {
        super.setFogging(i);
    }

    @Override
    public synchronized void setFoggingMode(int i) {
        super.setFoggingMode(i);
    }

    @Override
    public synchronized int getFogging() {
        return super.getFogging();
    }

    @Override
    public synchronized int getFoggingMode() {
        return super.getFoggingMode();
    }

    @Override
    public synchronized void setFogParameters(float v, float v1, float v2, float v3) {
        super.setFogParameters(v, v1, v2, v3);
    }

    @Override
    public synchronized void setFogParameters(float v, float v1, float v2, float v3, float v4) {
        super.setFogParameters(v, v1, v2, v3, v4);
    }

    @Override
    public synchronized void setClippingPlanes(float v, float v1) {
        super.setClippingPlanes(v, v1);
    }

    @Override
    public synchronized int checkCollision(com.threed.jpct.SimpleVector simpleVector, com.threed.jpct.SimpleVector simpleVector1, float v) {
        return super.checkCollision(simpleVector, simpleVector1, v);
    }

    @Override
    public synchronized com.threed.jpct.SimpleVector checkCollisionSpherical(com.threed.jpct.SimpleVector simpleVector, com.threed.jpct.SimpleVector simpleVector1, float v) {
        return super.checkCollisionSpherical(simpleVector, simpleVector1, v);
    }

    @Override
    public synchronized com.threed.jpct.SimpleVector checkCollisionEllipsoid(com.threed.jpct.SimpleVector simpleVector, com.threed.jpct.SimpleVector simpleVector1, com.threed.jpct.SimpleVector simpleVector2, int i) {
        return super.checkCollisionEllipsoid(simpleVector, simpleVector1, simpleVector2, i);
    }

    @Override
    public synchronized boolean checkCameraCollision(int i, float v) {
        return super.checkCameraCollision(i, v);
    }

    @Override
    public synchronized boolean checkCameraCollision(int i, float v, boolean b) {
        return super.checkCameraCollision(i, v, b);
    }

    @Override
    public synchronized boolean checkCameraCollision(int i, float v, float v1, boolean b) {
        return super.checkCameraCollision(i, v, v1, b);
    }

    @Override
    public synchronized boolean checkCameraCollision(com.threed.jpct.SimpleVector simpleVector, float v, float v1, boolean b) {
        return super.checkCameraCollision(simpleVector, v, v1, b);
    }

    @Override
    public synchronized java.lang.Object[] calcMinDistanceAndObject3D(com.threed.jpct.SimpleVector simpleVector, com.threed.jpct.SimpleVector simpleVector1, float v) {
        return super.calcMinDistanceAndObject3D(simpleVector, simpleVector1, v);
    }

    @Override
    public synchronized float calcMinDistance(com.threed.jpct.SimpleVector simpleVector, com.threed.jpct.SimpleVector simpleVector1, float v) {
        return super.calcMinDistance(simpleVector, simpleVector1, v);
    }

    @Override
    public synchronized boolean checkCameraCollisionSpherical(int i, float v, float v1, boolean b) {
        return super.checkCameraCollisionSpherical(i, v, v1, b);
    }

    @Override
    public synchronized boolean checkCameraCollisionSpherical(com.threed.jpct.SimpleVector simpleVector, float v, float v1, boolean b) {
        return super.checkCameraCollisionSpherical(simpleVector, v, v1, b);
    }

    @Override
    public synchronized boolean checkCameraCollisionEllipsoid(int i, com.threed.jpct.SimpleVector simpleVector, float v, int i1) {
        return super.checkCameraCollisionEllipsoid(i, simpleVector, v, i1);
    }

    @Override
    public synchronized boolean checkCameraCollisionEllipsoid(com.threed.jpct.SimpleVector simpleVector, com.threed.jpct.SimpleVector simpleVector1, float v, int i) {
        return super.checkCameraCollisionEllipsoid(simpleVector, simpleVector1, v, i);
    }

    @Override
    public synchronized void buildAllObjects() {
        super.buildAllObjects();
    }

    @Override
    public synchronized void createTriangleStrips() {
        super.createTriangleStrips();
    }

    @Override
    public synchronized void renderScene(com.threed.jpct.FrameBuffer frameBuffer) {
        super.renderScene(frameBuffer);
    }

    @Override
    public synchronized void draw(com.threed.jpct.FrameBuffer frameBuffer) {
        super.draw(frameBuffer);
    }

    @Override
    public synchronized void draw(com.threed.jpct.FrameBuffer frameBuffer, int i, int i1) {
        super.draw(frameBuffer, i, i1);
    }

    @Override
    public synchronized void drawWireframe(com.threed.jpct.FrameBuffer frameBuffer, java.awt.Color color) {
        super.drawWireframe(frameBuffer, color);
    }

    @Override
    public synchronized long getFrameCounter() {
        return super.getFrameCounter();
    }

    @Override
    public synchronized java.util.Enumeration getObjects() {
        return super.getObjects();
    }

    @Override
    public synchronized void setObjectsVisibility(boolean b) {
        super.setObjectsVisibility(b);
    }

    @Override
    public synchronized void removeAll() {
        super.removeAll();
    }

    @Override
    public synchronized void removeAllObjects() {
        super.removeAllObjects();
    }

    @Override
    public synchronized void removeAllLights() {
        super.removeAllLights();
    }

    @Override
    public synchronized void addPolyline(com.threed.jpct.Polyline polyline) {
        super.addPolyline(polyline);
    }

    @Override
    public synchronized void removePolyline(com.threed.jpct.Polyline polyline) {
        super.removePolyline(polyline);
    }

    @Override
    public synchronized void setGlobalShader(com.threed.jpct.GLSLShader glslShader) {
        super.setGlobalShader(glslShader);
    }

    @Override
    public synchronized com.threed.jpct.GLSLShader getGlobalShader() {
        return super.getGlobalShader();
    }

    @Override
    public synchronized java.lang.String toXML() {
        return super.toXML();
    }
}
