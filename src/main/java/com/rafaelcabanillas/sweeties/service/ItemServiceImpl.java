package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.*;
import com.rafaelcabanillas.sweeties.exception.ResourceNotFoundException;
import com.rafaelcabanillas.sweeties.model.Item;
import com.rafaelcabanillas.sweeties.model.Item.Size;
import com.rafaelcabanillas.sweeties.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public ItemDTO createItem(CreateItemDTO dto, String imageUrl, String imagePublicId, List<String> spriteUrls, List<String> spritePublicIds) {
        Item item = Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(imageUrl)
                .imagePublicId(imagePublicId)
                .materials(dto.getMaterials())
                .size(dto.getSize() != null ? dto.getSize().stream()
                        .map(sz -> Size.builder().alto(sz.getAlto()).ancho(sz.getAncho()).build()).toList()
                        : null)
                .sprites(spriteUrls)
                .spritesPublicIds(spritePublicIds)
                .isFeatured(dto.isFeatured())
                .isVisible(dto.isVisible())
                .build();
        itemRepository.save(item);
        return toItemDTO(item);
    }

    @Override
    public ItemDTO getItemById(Long id) {
        return itemRepository.findById(id)
                .map(this::toItemDTO)
                .orElseThrow(() -> new ResourceNotFoundException("El artículo no existe"));
    }

    @Override
    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll().stream().map(this::toItemDTO).toList();
    }

    @Override
    public ItemDTO updateItem(Long id, UpdateItemDTO dto, String imageUrl, String imagePublicId, List<String> spriteUrls, List<String> spritePublicIds) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El artículo no existe"));

        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getPrice() != null) item.setPrice(dto.getPrice());
        if (dto.getMaterials() != null) item.setMaterials(dto.getMaterials());
        if (dto.getSize() != null) item.setSize(dto.getSize().stream()
                .map(sz -> Size.builder().alto(sz.getAlto()).ancho(sz.getAncho()).build()).toList());
        // Sprites logic (replace if new ones were sent)
        if (spriteUrls != null && !spriteUrls.isEmpty()) item.setSprites(spriteUrls);
        if (spritePublicIds != null && !spritePublicIds.isEmpty()) item.setSpritesPublicIds(spritePublicIds);

        // Main image
        if (imageUrl != null && !imageUrl.isEmpty()) item.setImageUrl(imageUrl);
        if (imagePublicId != null && !imagePublicId.isEmpty()) item.setImagePublicId(imagePublicId);

        item.setFeatured(dto.isFeatured());
        item.setVisible(dto.isVisible());

        itemRepository.save(item);
        return toItemDTO(item);
    }

    @Override
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("El artículo no existe");
        }
        itemRepository.deleteById(id);
    }

    private ItemDTO toItemDTO(Item item) {
        return ItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .imagePublicId(item.getImagePublicId())
                .materials(item.getMaterials())
                .size(item.getSize() != null ? item.getSize().stream()
                        .map(sz -> SizeDTO.builder().alto(sz.getAlto()).ancho(sz.getAncho()).build()).toList() : null)
                .sprites(item.getSprites())
                .spritesPublicIds(item.getSpritesPublicIds())
                .isFeatured(item.isFeatured())
                .isVisible(item.isVisible())
                .build();
    }
}
