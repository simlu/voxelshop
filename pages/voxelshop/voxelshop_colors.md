---
title: VoxelShop Color
tags: [color]
keywords:
summary: "Presentation of all available color related features and frames"
sidebar: voxelshop_sidebar
permalink: voxelshop_colors.html
folder: voxelshop
---

VoxelShop provides color modification for the scene's background and model.

## Background Color
IMG Allows to select the background color for the viewing ports, i.e. side views and 3D View. This is helpful to get better contrast between the voxels and the background.

## Model Color - Frames
The color model can be defined with multiple tools.

### Quick Color Chooser
IMG Allows to select the color in a predefined set. The chosen color becomes the current one used to add voxels in the scene.

### Color Palette
IMG
This window allows you to manage your own color palette. This is very useful if you want to keep a consistent style and makes reusing colors very easy.

You can replace colors by first unlocking the palette and then using
- CTRL + LEFT CLICK: Replace clicked color with currently selected color.
- CTRL + RIGHT CLICK: Erase clicked color.
If you want to select a color from the palette simply use LEFT CLICK or use WASD (default shortcuts) to move between selected colors.

### Color Picker
IMG
This window allows you to quickly select a color. You can select the hue on the right. The selected color is outlined with a circle.

### Color Slider
IMG
This window allows you to select your current color by using different color models and sliders. The three color models that can be used are CMYK, HSB and RGB.

#### RGB Tab
IMG
This tab contains the three sliders Red, Green and Blue which form the RGB color model.

#### CMYK Tab
IMG
This tab contains the four sliders Cyan, Magenta, Yellow and Key (Black) which form the CMYK color model.

#### HSB Tab
IMG
This tab contains the three sliders Hue, Saturation and Brightness which form the HSB color model.

### Hex Picker Window
IMG
This windows allows you to enter a color as a hex value. You can even enter shortened hex values, e.g. AA or FFF.

### Color Adjuster Window
IMG
This windows allows you to adjust the colors of all selected Voxel.

## Model Color - Tools

### Color Picker Tool
IMG This tool allows you to select a color from existing voxels.

|Key|Description|
|:--:|---|
|**LMC**| Select color/texture of clicked voxel.|
|**RMC**| Select color/texture of clicked voxel if the voxel is in **the selected layer**.|
|**SHIFT + LMC**| Draw out an area to select the (weighted) average color of all voxels inside.|
|**SHIFT + RMC**| Like LMC, but each unique color is only used once.|

### Flood Fill Tool
IMG Recolor all voxels that have the same color as the selected one using the currently selected color.

|Key|Description|
|:--:|---|
|**LMC**| Fill all attached voxels with the same color with the selected color.|
|**RMC**| Fill all attached voxels with the same color and in **the selected layer** with the selected color.|
|**SHIFT + LMC**| Fill all voxels with the same color with the selected color.|
|**SHIFT + RMC**| Fill all voxels with the same color and in **the selected layer** with the selected color.|

> Note: In side view this works layerwise

### Color Changer Tool
IMG Change the color of the clicked voxel into the currently selected color.

|Key|Description|
|:--:|---|
|**LMC**| Change the color/texture of the clicked voxel. If the texture is already the selected one for the clicked side of the voxel, is is rotated.|
|**RMC**| Change the color/texture of the clicked voxel if the voxel is in **the selected layer**. If the texture is already the selected one for the clicked side of the voxel, is is mirrored (it doesn't matter if the voxel is in **the selected layer** in this case).|
|**SHIFT + LMC**| Draw out an area to set the color/texture of all voxels in that area.
|**SHIFT + RMC**| Like LMC, but only considers voxels in **the selected layer**.|