/*
*  This file is part of OpenDS (Open Source Driving Simulator).
*  Copyright (C) 2015 Rafael Math
*
*  OpenDS is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  OpenDS is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with OpenDS. If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.opends.oculusRift;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;

import eu.opends.oculusRift.post.BarrelDistortionFilter;
import eu.opends.oculusRift.post.BasicSSAO;

import com.jme3.post.Filter;
import com.jme3.post.SceneProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.scene.control.CameraControl;
import com.jme3.shadow.DirectionalLightShadowFilter;
import java.util.List;

/**
 *
 * @author reden
 */
public class StereoCamAppState extends AbstractAppState {
    
    private SimpleApplication app;
    private FilterPostProcessor ppLeft, ppRight;
    private BarrelDistortionFilter filterLeft, filterRight;
    Camera camLeft,camRight,guiCamLeft,guiCamRight;
    ViewPort viewPortLeft, viewPortRight, guiViewPortRight;
    private StereoCameraControl camControl = new StereoCameraControl();
    private HMDInfo info;
    private boolean flipEyes;
    private float guiDistance;
    
    public StereoCamAppState(float guiDistance, boolean flipEyes) {
        this.flipEyes = flipEyes;
        this.guiDistance = guiDistance;
    }
    
    public StereoCamAppState(float guiDistance) {
        flipEyes = false;
        this.guiDistance = guiDistance;
    }
    
    public StereoCamAppState() {
        flipEyes = false;
        guiDistance = 0.045f;
    }   
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;

        viewPortLeft = app.getViewPort();
        camLeft = app.getCamera();
        if(camControl != null){
            camControl.setCamera(camLeft);
        }
        
        if(camRight == null){
            camRight = camLeft.clone();
            camControl.setCamera2(camRight);
            
        }
        camControl.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camLeft.setViewPort(0.0f, 0.5f, 0.0f, 1.0f);
        camRight.setViewPort(0.5f, 1f, 0.0f, 1f);
        viewPortRight = app.getRenderManager().createPostView("Right viewport", camRight);
        viewPortRight.setClearFlags(true, true, true);
        viewPortRight.attachScene(((SimpleApplication)app).getRootNode());

        info = OculusRift.getHMDInfo();            
        
        filterRight =new BarrelDistortionFilter(info, false);
        filterLeft=new BarrelDistortionFilter(info, true);
        ppRight =new FilterPostProcessor(app.getAssetManager());
        for(SceneProcessor sceneProcessor : viewPortLeft.getProcessors()){
            if(sceneProcessor instanceof FilterPostProcessor){
                ppLeft = (FilterPostProcessor) sceneProcessor;
                break;
            }
        }
        if(ppLeft == null){
            ppLeft =new FilterPostProcessor(app.getAssetManager());
            viewPortLeft.addProcessor(ppLeft);
        }
        
        ppRight.addFilter(filterRight);
        ppLeft.addFilter(filterLeft);
        viewPortRight.addProcessor(ppRight);
        
        
        float offset = info.getInterpupillaryDistance() * 0.5f;
        camControl.setCamHalfDistance(offset);
        setupGuiViewports();
        
        cloneProcessors();        
        if( flipEyes ) camControl.swapCameras();
    }
    
   
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
    }
    
    public void setCameraControl(StereoCameraControl control){
        this.camControl = control;
        camRight = control.getCamera2();
    }
    
    public StereoCameraControl getCameraControl(){
        return camControl;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        OculusRift.destroy();
    }
    
    public void setGuiDistance(float newGuiDistance) {
        guiDistance = newGuiDistance;
        guiCamLeft.setViewPort(0.0f + guiDistance, 0.5f + guiDistance, 0.0f, 1.0f);
        guiCamRight.setViewPort(0.5f - guiDistance, 1f - guiDistance, 0.0f, 1f); // l,r,b,t        
    }
    
    public void adjustGuiDistance(float adjustAmount) {
        setGuiDistance(guiDistance + adjustAmount);
    }
    
    private void setupGuiViewports(){
        ViewPort guiViewPortLeft = app.getGuiViewPort();
        
        guiCamLeft = guiViewPortLeft.getCamera();
        guiCamRight = guiCamLeft.clone();
        
        setGuiDistance(guiDistance);        
        
        guiViewPortRight = app.getRenderManager().createPostView("Gui Default Right", guiCamRight);
        guiViewPortRight.setClearFlags(false, false, false);
        guiViewPortRight.attachScene(((SimpleApplication)app).getGuiNode());

    }
    
    public void cloneProcessors(){
        List<SceneProcessor> processors = viewPortLeft.getProcessors();
        for(SceneProcessor sp: processors){
            if(sp instanceof FilterPostProcessor){
                FilterPostProcessor fpp1 = (FilterPostProcessor) sp;
                BarrelDistortionFilter bdf = ppRight.getFilter(BarrelDistortionFilter.class);
                ppRight.removeFilter(bdf);
                for(Filter filter: fpp1.getFilterList()){
                                        
                    Filter f2 = null;
                    if(filter instanceof FogFilter){
                        f2 = FilterUtil.cloneFogFilter((FogFilter)filter);
                        
                    } 
                    else if (filter instanceof BasicSSAO) {
                        f2 = new BasicSSAO((BasicSSAO)filter);
                    }
                    //else if (filter instanceof WaterFilter){
                        //f2 = ((WaterFilter)filter). //doesn't seem to be a clone function ready to go?
                    //} 
                    else if (filter instanceof SSAOFilter){
                        f2 = FilterUtil.cloneSSAOFilter((SSAOFilter)filter);
                    } else if (filter instanceof DirectionalLightShadowFilter){
                        f2 = FilterUtil.cloneDirectionalLightShadowFilter(app.getAssetManager(), (DirectionalLightShadowFilter)filter);
                    } 
                    else if (!(filter instanceof BarrelDistortionFilter)){
                        f2 = filter; // dof, bloom, lightscattering
                    }
                    
                    if(f2 != null) ppRight.addFilter(f2);                    
                }
                ppRight.addFilter(bdf);
            } else if (sp instanceof OculusDirectionalLightShadowRenderer){
                OculusDirectionalLightShadowRenderer dlsr = (OculusDirectionalLightShadowRenderer) sp;
                
                OculusDirectionalLightShadowRenderer dlsrRight = dlsr.clone();
                dlsrRight.setLight(dlsr.getLight());
                
                viewPortRight.getProcessors().add(0, dlsrRight);
            }
        }
    }
    
    public ViewPort getLeftViewPort(){
        return viewPortLeft;
    }
    
    public ViewPort getRightViewPort(){
        return viewPortRight;
    }
    
}
