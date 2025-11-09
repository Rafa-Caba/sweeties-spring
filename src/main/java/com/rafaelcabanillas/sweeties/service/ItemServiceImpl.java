package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.CreateItemDTO;
import com.rafaelcabanillas.sweeties.dto.ItemDTO;
import com.rafaelcabanillas.sweeties.dto.SizeDTO;
import com.rafaelcabanillas.sweeties.dto.UpdateItemDTO;
import com.rafaelcabanillas.sweeties.exception.ResourceNotFoundException;
import com.rafaelcabanillas.sweeties.model.Item;
import com.rafaelcabanillas.sweeties.model.Item.Size;
import com.rafaelcabanillas.sweeties.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    /* ===================== CREATE ===================== */

    @Override
    @Transactional
    public ItemDTO createItem(CreateItemDTO dto,
                              String imageUrl,
                              String imagePublicId,
                              List<String> spriteUrls,
                              List<String> spritePublicIds) {

        boolean featured = dto.getIsFeatured() != null ? dto.getIsFeatured() : false;
        boolean visible  = dto.getIsVisible()  != null ? dto.getIsVisible()  : true;

        Item item = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(imageUrl)
                .imagePublicId(imagePublicId)
                .materials(dto.getMaterials()) // This is OK (CreateItemDTO defaults to ArrayList)
                .size(dto.getSize() != null
                        ? dto.getSize().stream()
                        .map(s -> Size.builder()
                                .alto(s.getAlto())
                                .ancho(s.getAncho())
                                .build())
                        .collect(Collectors.toList()) // <-- FIX 1: Use .collect(Collectors.toList())
                        : new ArrayList<>())          // <-- FIX 2: Use new ArrayList<>()
                .sprites(nullSafe(spriteUrls))
                .spritesPublicIds(nullSafe(spritePublicIds))
                .isFeatured(featured)
                .isVisible(visible)
                .build();

        itemRepository.save(item);
        return toItemDTO(item);
    }

    /* ===================== READ ===================== */

    @Override
    public ItemDTO getItemById(Long id) {
        return itemRepository.findById(id)
                .map(this::toItemDTO)
                .orElseThrow(() -> new ResourceNotFoundException("El artículo no existe"));
    }

    @Override
    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::toItemDTO)
                .toList();
    }

    /* ===================== UPDATE ===================== */

    @Override
    @Transactional
    public ItemDTO updateItem(Long id,
                              UpdateItemDTO dto,
                              String imageUrl,
                              String imagePublicId,
                              List<String> spriteUrls,
                              List<String> spritePublicIds) {

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El artículo no existe"));

        // Basic fields: only update when provided
        if (dto.getName() != null)        item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getPrice() != null)       item.setPrice(dto.getPrice());
        if (dto.getMaterials() != null)   item.setMaterials(dto.getMaterials());

        // Size: replace full list when provided
        if (dto.getSize() != null) {
            item.setSize(dto.getSize().stream()
                    .map(s -> Size.builder()
                            .alto(s.getAlto())
                            .ancho(s.getAncho())
                            .build())
                    .collect(Collectors.toList())); // <-- FIX 4: Use .collect(Collectors.toList())
        }

        // Sprites semantics:
        //   null -> no change
        //   []   -> clear list
        //   list -> replace list
        if (spriteUrls != null)       item.setSprites(spriteUrls);
        if (spritePublicIds != null)  item.setSpritesPublicIds(spritePublicIds);

        // Main image (null = no change)
        if (imageUrl != null && !imageUrl.isBlank())         item.setImageUrl(imageUrl);
        if (imagePublicId != null && !imagePublicId.isBlank()) item.setImagePublicId(imagePublicId);

        // Visibility flags (Boolean wrappers)
        if (dto.getIsFeatured() != null) item.setFeatured(dto.getIsFeatured());
        if (dto.getIsVisible()  != null) item.setVisible(dto.getIsVisible());

        itemRepository.save(item);
        return toItemDTO(item);
    }

    /* ===================== DELETE ===================== */

    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("El artículo no existe");
        }
        itemRepository.deleteById(id);
    }

    /* ===================== Helpers ===================== */

    private ItemDTO toItemDTO(Item item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .imagePublicId(item.getImagePublicId())
                .materials(item.getMaterials())
                .size(item.getSize() != null
                        ? item.getSize().stream()
                        .map(sz -> SizeDTO.builder()
                                .alto(sz.getAlto())
                                .ancho(sz.getAncho())
                                .build())
                        .toList()
                        : Collections.emptyList())
                .sprites(item.getSprites())
                .spritesPublicIds(item.getSpritesPublicIds())
                .isFeatured(item.isFeatured())
                .isVisible(item.isVisible())
                .build();
    }

    private static <T> List<T> nullSafe(List<T> list) {
        // Always return a mutable list for JPA
        return list == null ? new ArrayList<>() : list;
    }
}
