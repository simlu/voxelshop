---
title: VoxelShop Frames
tags: [frames]
keywords:
summary: "Presentation of all available frames"
sidebar: voxelshop_sidebar
permalink: voxelshop_frames.html
folder: voxelshop
---

TODO: Overview of all the frames with links to the detail pages for the frames

---
VoxelShop is based on frames and complementary tools to provide the best usage experience.

Frames are related to:

- 2D and 3D Viewing/Editing
- Color management
- Shortcuts management

## Axis Views

![Side View](./images/Screenshots/side-view-screenshot.png)

VoxelShop provides 3 views for the main 3D axis:
-  Front - XY View
-  Top - XZ View
-  Left - YZ View

Each view shows one slice of your model in the corresponding axis.
You can change the plane level with ![Move In](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/sideview/move_in.png?raw=true) and ![Move out](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/sideview/move_out.png?raw=true) to respectively going forward or backward.
You can reset the plane to the origin one with ![Origin](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/sideview/make_zero.png?raw=true).

You can pedestal and truck the camera by pressing CTRL and dragging the mouse.
To reset the view you can click on ![Reset](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/sideview/reset.png?raw=true).
Zoom In / Zoom Out can be done also with ![Plus](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/sideview/plus.png?raw=true) and ![Minus](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/sideview/minus.png?raw=true).

To show a specific plane in this view you can select the camera tool, press shift and click a voxel in the 3D View that is in the plane that you want to display.

When working in this window the position and plane are highlighted in the 3D View. You can align the 3D view with the current plane with ![Align](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/sideview/align.png?raw=true).

During edition it could be necessary to have the plane view maximized. You can then switch with the 3D view with ![](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/view/xySwap.png?raw=true)/![XZ](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/view/xzSwap.png?raw=true)/![YZ](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/view/yzSwap.png?raw=true). The current plane view is replaced by the 3D one.

During manipulation, the 3D cursor location is displayed in the right up corner of the view. This provides the 3D location where you can create/remove voxel.

## 3D View

![3D View](./images/Screenshots/mainview-screenshot.png)

This sub-window shows your creation as a whole and can be used to intuitively change voxels. The coordinate axis in the top left corner shows the current orientation of the voxel object.

During manipulation, the 3D cursor location is displayed in the right up corner of the view. This provides the 3D location where you can create/remove voxel.

### Manipulation

You can rotate the camera around the current rotation center by left clicking the background (or holding CTRL) and dragging the mouse. Using the right click instead will pedestal and truck the camera.

![Reset](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/mainview/reset.png?raw=true) Reset the camera position for this sub-window.

Zoom In / Zoom Out can be done also with ![Plus](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/mainview/plus.png?raw=true) and ![Minus](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/mainview/minus.png?raw=true).

> Note: Alternatively you can use the mouse wheel.

### Optimisation

![Wireframe](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/mainview/wireframe.png?raw=true) will activate a flag to draw the wireframe of the object. This is useful for detecting holes in your creation.

> Note: A voxel object is optimized if it is filled with voxels!

![Fill](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/mainview/fill.png?raw=true) allows to fill any caves in your Voxel model with Voxel. Rendering only the hull can be more efficient for the graphic engine. Works across Layers.

![Hollow](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/mainview/hollow.png?raw=true) allows to remove any interior Voxel. This will usually result in smaller exported files, however Voxel engines might render the interior. Works across Layers.

### View Swapping
![XY](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/view/xySwap.png?raw=true) Swap the position of this sub-window with the XY View window.

![XZ](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/view/xzSwap.png?raw=true) Swap the position of this sub-window with the XZ View window.

![YZ](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/view/yzSwap.png?raw=true) Swap the position of this sub-window with the YZ View window.

## Color
Multiple frames are related to color definition and management. You can find details [here](voxelshop-colors.html).

## Layers
![Layers View](./images/Screenshots/layer-screenshot.png)

3D model creation is made usually with multiple parts and the process is easier with this separation feature. In VoxelShop, this is called **Layer**.

You can create multiple layers and restrict tool usage to selected layer.

Layers can be hidden to efficiently work on large projects with many voxels. They furthermore allow for easy modularity. You can conveniently swap parts of existing objects by hiding them and then creating a replacement in a new layer.

Layers can be renamed by double clicking them and their visibility can be changed by clicking the visibility information (right column). A single click onto the layer name will select the layer.

![Merge](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/layer/merge.png?raw=true) Merge all visible layers into a new layer.

![Down](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/layer/move_down.png?raw=true) Move the currently selected layer one row down.

![Up](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/layer/move_up.png?raw=true) Move the currently selected layer one row up.

![Remove](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/layer/remove.png?raw=true) Delete the currently selected layer.

![Add](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/layer/add.png?raw=true) Add a new layer. You can rename layers by double clicking their name.

## Shortcut Manager
![Shortcuts View](./images/Screenshots/shortcut-manager-screenshot.png)

This sub-window allows you to freely bind all your shortcuts. It is organized by sub-window. Shortcuts that always work are listed in the "Global" tab. Shortcuts that only work when the corresponding sub-window is active are listed in their separate tabs.

The shortcuts are not restricted to include modifies, so you can even bind single letters. Shortcuts can be easily changed by double clicking them and then pressing the desired, available shortcut.

