---
title: VoxelShop Tools
tags: [tools]
keywords:
summary: "Presentation of all available tools"
sidebar: voxelshop_sidebar
permalink: voxelshop_tools.html
folder: voxelshop
---

TODO: Document tools, quick example, document related shortcuts

----

VoxelShop tools are available with related toolbars or keys activation (keyboard + mouse). 

You can have a quick overview of all keys [here](Keys).

Each frames provide a dynamic help icon **?** in the title bar. You can click on it and then move your mouse to desired element to get documentation related to it.

## Environment Tool
The creation environment can be configured with lighting, display helpers like borders or grid, background color...

### ![Dynamic Lighting](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/option_bar/light.png?raw=true) Lighting Modes
Choose between two different light modes:

- **Static lighting** will place two light sources around the object.
- **Dynamic lighting** will place a negative light source to where the camera is. 

While working on voxels, the dynamic lighting is usually preferred.


### ![Render Quality](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/option_bar/quality.png?raw=true) Render Quality
 Change the rendering quality.

The low quality is much faster, but the detail quality is not as good. This is especially visible when displaying the voxel edges.

### ![Outline Edges](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/option_bar/grid.png?raw=true) Outline Edges
 This flag outlines the edges of your voxels.

This is useful for distinguishing voxels better. The edges look better if the high rendering quality is selected.

### ![Bounding Box](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/option_bar/boundingBox.png?raw=true) Bounding Box
 Toggle the bounding box.

The bounding box allows you to draw directly into the 3D View and can be helpful to get the distances right for smaller objects.

The size of the bounding box can be adjusted in all three dimensions with the arrow bellow 
the icon.

### ![Mirror Flag](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/option_bar/mirrorflag.png?raw=true) Mirror Flag
 If this flag is active, every action that affects voxels is mirrored. This is useful for creating symmetric objects.

> **IMPORTANT**: The feature is still experimental.

### ![Background Color](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/framebars/mainview/color_bg.png?raw=true) Background Color
Select the background color for the viewing ports, i.e. side views and 3D View. This is helpful to get better contrast between the voxels and the background.

The color selection is done with a predefined color wheel.

## Edition Tools

### ![Camera Tool](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/tool_bar/view.png?raw=true) Camera Tool
 If this tool is selected, the camera is always enabled (otherwise it is only enabled if you click the background or hold CTRL).

|Key|Description|
|:--:|---|
|**LMC**|Rotates view around current rotation center (3D View), moves view (side view).|
|**RMC**| Moves camera up/down along y-axis (main view).|
|**SHIFT + LMC**| Aligns all side views to the clicked voxel.|
|**SHIFT + RMC**| As LMC, but also sets the rotation point of the 3D View to the clicked voxel. You can reset the rotation point in the toolbar of the 3D View.|

### ![Draw Tool](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/tool_bar/draw.png?raw=true) Draw Tool
 
|Key|Description|
|:--:|---|
|**LMC**| Draws a voxel with the current color/texture.|
|**RMC**| Erases the selected voxel.|
|**SHIFT + LMC**| Draws out an area and fills it with voxel. |
|**SHIFT + RMC**| Draws out an area and erases all voxels inside.|

### ![Erase Tool](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/tool_bar/erase.png?raw=true)Erase Tool

|Key|Description|
|:--:|---|
|**LMC**| Erase clicked voxel.|
|**RMC**| Erase clicked voxel, but only if in selected layer.|
|**SHIFT + LMC**| Draw out an area and erase all voxels inside.|
|**SHIFT + RMC**| Draw out an area and erase all voxels that are inside and also in **the selected layer**.|

### ![Color Picker](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/tool_bar/color_picker.png?raw=true) Color Picker
 This tool allows you to select a color from existing voxels.

|Key|Description|
|:--:|---|
|**LMC**| Select color/texture of clicked voxel.|
|**RMC**| Select color/texture of clicked voxel if the voxel is in **the selected layer**.|
|**SHIFT + LMC**| Draw out an area to select the (weighted) average color of all voxels inside.|
|**SHIFT + RMC**| Like LMC, but each unique color is only used once.|

### ![Flood Fill](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/tool_bar/floodfill.png?raw=true) Flood Fill

Recolor all voxels that have the same color as the selected one using the currently selected color.


|Key|Description|
|:--:|---|
|**LMC**| Fill all attached voxels with the same color with the selected color.|
|**RMC**| Fill all attached voxels with the same color and in **the selected layer** with the selected color.|
|**SHIFT + LMC**| Fill all voxels with the same color with the selected color.|
|**SHIFT + RMC**| Fill all voxels with the same color and in **the selected layer** with the selected color.|

> **Note**: In side view this works layerwise

### ![Color Change](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/tool_bar/color_changer.png?raw=true) Color Changer Tool

Change the color of the clicked voxel into the currently selected color.

|Key|Description|
|:--:|---|
|**LMC**| Change the color/texture of the clicked voxel. If the texture is already the selected one for the clicked side of the voxel, is is rotated.|
|**RMC**| Change the color/texture of the clicked voxel if the voxel is in **the selected layer**. If the texture is already the selected one for the clicked side of the voxel, is is mirrored (it doesn't matter if the voxel is in **the selected layer** in this case).|
|**SHIFT + LMC**| Draw out an area to set the color/texture of all voxels in that area.
|**SHIFT + RMC**| Like LMC, but only considers voxels in **the selected layer**.|

## Selection Tools
VoxelShop provides multiple selection and copy/paste/move features.

### ![Select](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/tool_bar/select_tool.png?raw=true) Select Tool
 This tool allows you to select many voxels at once. Selected voxels can be batch processed using the selection tool bar.

|Key|Description|
|:--:|---|
|**LMC**| Select clicked voxel.|
|**RMC**| Deselect clicked voxel.|
|**LMP + Drag**| Select voxel in the drawn area.|
|**RMP + Drag**| Deselect voxel in the drawn area.|
|**SHIFT + LMP + Drag**| Draw out an area to select all voxels inside it.|
|**SHIFT + RMP + Drag**| Draw out an area to deselect all voxels inside it.|

### ![Cut Selection](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/cut.png?raw=true) Cut Selection
Copy and delete the selected, visible voxels.

### ![Copy Selection](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/copy.png?raw=true) Copy Selection
 Copy the selected, visible voxels.

### ![Paste Voxel](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/paste.png?raw=true) Paste Voxel
 Paste previously copied voxels into the currently selected layer.

### ![Move Selection](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/finalize_move.png?raw=true) Move Selection
 By dragging the selection with the mouse or using the arrow and page keys (default shortcuts), the visible selection can be moved. This action finalized the moving.

### ![Clone Selection](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/finalize_move_as_copy.png?raw=true) Clone Selection
 By dragging the selection with the mouse or using the arrow and page keys (default shortcuts), the selection can be moved. This action clones the visible selection to the new position instead of moving it.

### ![Deselect Selection](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/deselect.png?raw=true)  Deselect Selection
This action deselects all currently selected and visible voxels.

### ![Delete Selection](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/delete_select.png?raw=true) Delete Selection
 This action deletes the visible and selected voxels.

### ![Expand Selection](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/expand_selection.png?raw=true) Expand Selection
 This action expands the selection to all Voxel with the same color as any currently selected Voxel. If no Voxel is selected it uses the currently selected color. Works across layers.

### ![Recolor Selection](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/recolor.png?raw=true) Recolor Selection
 This action colors all selected, visible voxels using the currently selected color.

### ![Retexture Selection](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/retexture.png?raw=true) Retexture Selection
 This action textures all selected, visible voxels with the currently selected texture. If no texture is selected all textures are removed (and the color of the voxel is displayed again).

## Layers Tools

### ![Select All](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/select_layer.png?raw=true) Select All Voxels

### ![Select Layer](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/select_layer.png?raw=true) Select Layer
This action selects all voxels in the current layer.

### ![Move to new layer](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/as_new_layer.png?raw=true) Move to New Layer

This action creates a new layer and moves all visible, selected voxels into it.

## Rotation Tools

### ![Rotate X](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/rotate/rotatex.png?raw=true) Rotate Around X Axis
 This button allows you to rotate the selection by 90, 180 or 270 degrees around the X axis and the weighted center.

Note: The color of this button corresponds to the axis displayed in the view windows.

### ![Rotate Y](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/rotate/rotatey.png?raw=true) Rotate Around Y Axis
 This button allows you to rotate the selection by 90, 180 or 270 degrees around the Y axis and the weighted center.

Note: The color of this button corresponds to the axis displayed in the view windows.

### ![Rotate Z](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/rotate/rotatez.png?raw=true)  Rotate Around Z Axis
This button allows you to rotate the selection by 90, 180 or 270 degrees around the Z axis and the weighted center.

Note: The color of this button corresponds to the axis displayed in the view windows.

## Mirror Tools

### ![Mirror X](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/mirror/mirrorx.png?raw=true) Mirror Against YZ Plane
 This action mirrors the selection against the YZ plane and the weighted center.

Note: The color of this button corresponds to the axis displayed in the view windows.

### ![Mirror Y](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/mirror/mirrory.png?raw=true) Mirror Against XZ Plane
 This action mirrors the selection against the XZ plane and the weighted center.

Note: The color of this button corresponds to the axis displayed in the view windows.

### ![Mirror Z](https://github.com/simlu/voxelshop/blob/master/PS4k/resource/img/bars/select_bar/mirror/mirrorz.png?raw=true) Mirror Against XY Plane
 This action mirrors the selection against the XY plane and the weighted center.

Note: The color of this button corresponds to the axis displayed in the view windows.