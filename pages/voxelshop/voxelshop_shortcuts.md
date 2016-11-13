---
title: VoxelShop Shortcuts
tags: [shortcuts]
keywords:
summary: "Presentation of all available shortcuts"
sidebar: voxelshop_sidebar
permalink: voxelshop_shortcuts.html
folder: voxelshop
---

This section provides all the default action key assignments. All keys can be freely bound in the shortcut manager. Note that frame shortcuts are only available when the corresponding frame is active.

|Key|Description|
|---|---|
|LMC|Left Mouse Click|
|RMC|Right Mouse Click|
|LMP|Left Mouse Pressed|
|RMP|Right Mouse Pressed|

## General

|Key|Description|
|:---:|---|
|**CTRL** or **ALT** |Enable the free view, no matter which tool you are using. Use the mouse to move around the model.|

## Camera Tool

|Key|Description|
|:--:|---|
|**LMC**|Rotates view around current rotation center (main view), moves view (side view).|
|**RMC**| Moves camera up/down along y-axis (main view).|
|**SHIFT + LMC**| Aligns all side views to the clicked voxel.|
|**SHIFT + RMC**| As LMC, but also sets the rotation point of the main view to the clicked voxel. You can reset the rotation point in the toolbar of the main view.|

## Draw Tool

|Key|Description|
|:--:|---|
|**LMC**| Draws a voxel with the current color/texture.|
|**RMC**| Erases the selected voxel.|
|**SHIFT + LMC**| Draws out an area and fills it with voxel. |
|**SHIFT + RMC**| Draws out an area and erases all voxels inside.|

## Erase Tool

|Key|Description|
|:--:|---|
|**LMC**| Erase clicked voxel.|
|**RMC**| Erase clicked voxel, but only if in selected layer.|
|**SHIFT + LMC**| Draw out an area and erase all voxels inside.|
|**SHIFT + RMC**| Draw out an area and erase all voxels that are inside and also in **the selected layer**.|

## Color Picker

|Key|Description|
|:--:|---|
|**LMC**| Select color/texture of clicked voxel.|
|**RMC**| Select color/texture of clicked voxel if the voxel is in **the selected layer**.|
|**SHIFT + LMC**| Draw out an area to select the (weighted) average color of all voxels inside.|
|**SHIFT + RMC**| Like LMC, but each unique color is only used once.|

## Flood Fill

|Key|Description|
|:--:|---|
|**LMC**| Fill all attached voxels with the same color with the selected color.|
|**RMC**| Fill all attached voxels with the same color and in **the selected layer** with the selected color.|
|**SHIFT + LMC**| Fill all voxels with the same color with the selected color.|
|**SHIFT + RMC**| Fill all voxels with the same color and in **the selected layer** with the selected color.|

> Note: In side view this works layerwise

## Color Changer Tool

|Key|Description|
|:--:|---|
|**LMC**| Change the color/texture of the clicked voxel. If the texture is already the selected one for the clicked side of the voxel, is is rotated.|
|**RMC**| Change the color/texture of the clicked voxel if the voxel is in **the selected layer**. If the texture is already the selected one for the clicked side of the voxel, is is mirrored (it doesn't matter if the voxel is in **the selected layer** in this case).|
|**SHIFT + LMC**| Draw out an area to set the color/texture of all voxels in that area.
|**SHIFT + RMC**| Like LMC, but only considers voxels in **the selected layer**.|

## Select Tool

|Key|Description|
|:--:|---|
|**LMC**| Select clicked voxel.|
|**RMC**| Deselect clicked voxel.|
|**LMP + Drag**| Select voxel in the drawn area.|
|**RMP + Drag**| Deselect voxel in the drawn area.|
|**SHIFT + LMP + Drag**| Draw out an area to select all voxels inside it.|
|**SHIFT + RMP + Drag**| Draw out an area to deselect all voxels inside it.|