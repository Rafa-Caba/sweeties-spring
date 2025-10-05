package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.*;
import java.util.List;

public interface ItemService {
    ItemDTO createItem(CreateItemDTO dto, String imageUrl, String imagePublicId, List<String> spriteUrls, List<String> spritePublicIds);
    ItemDTO getItemById(Long id);
    List<ItemDTO> getAllItems();
    ItemDTO updateItem(Long id, UpdateItemDTO dto, String imageUrl, String imagePublicId, List<String> spriteUrls, List<String> spritePublicIds);
    void deleteItem(Long id);
}