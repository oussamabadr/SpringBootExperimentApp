/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.badr.ordermanagement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import javax.validation.Valid;
import org.badr.ordermanagement.entity.AbstractBaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author OBD
 * @param <Entity>
 */
public abstract class AbstractControllerWithUUID<Entity extends AbstractBaseEntity> {
	
	private CrudRepository<Entity, UUID> crudRepository;
	
	@Autowired private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;
	
	private final String ID_ENTITY = "entityID";
	
	private final String ENTITY_ID_PATH = "/{entityID}";

	private Class<Entity> targetEntityClass;
	
	
	protected abstract void setRequiredVaraible();

	
	protected void setCrudRepository(CrudRepository<Entity, UUID> crudRepository) {
		this.crudRepository = crudRepository;
	}

	public void setTargetEntityClass(Class<Entity> targetEntityClass) {
		this.targetEntityClass = targetEntityClass;
	}	
	
	
	@GetMapping
	public ResponseEntity<?> getAllEntities(){
		return ResponseEntity.ok(crudRepository.findAll());
	}
	
	@GetMapping(path = ENTITY_ID_PATH)
	public ResponseEntity<?> getCategoryById(@PathVariable(ID_ENTITY) Entity entity){		
		return ResponseEntity.ok(entity);
	}	
	
	@GetMapping(params = ID_ENTITY)
	public ResponseEntity<?> getCategoryByIdParam(@RequestParam(ID_ENTITY) Entity entity){		
		return ResponseEntity.ok(entity);
	}
	
	@PostMapping
	public ResponseEntity<?> addCategory(@Valid @RequestBody Entity transientEntity){
		
		Entity entity =  crudRepository.save(transientEntity);

		URI location = ControllerLinkBuilder.linkTo(this.getClass()).slash(entity.getId()).toUri();		
		return ResponseEntity.created(location).body(entity);
	}
	
	@DeleteMapping(path = ENTITY_ID_PATH)
	public ResponseEntity<?> deleteCategory(@PathVariable UUID entityID){		
		crudRepository.delete(entityID);
		
		return ResponseEntity.noContent().build();
	}
	
	@PatchMapping(path = ENTITY_ID_PATH)
	public ResponseEntity<?> patchCategory(@PathVariable(ID_ENTITY) Entity oldEntity, @RequestBody String dataToPatch){
		
		HttpStatus httpStatus = HttpStatus.NOT_FOUND;		
		
		Entity newEntity = null;
		
		if (oldEntity != null){
			newEntity = applyPatch(oldEntity, dataToPatch);
			newEntity	= crudRepository.save(newEntity);
			httpStatus = HttpStatus.OK;
		}
		
		return new ResponseEntity<>(newEntity, httpStatus);
	}
	
	@PutMapping
	public ResponseEntity<?> putCategory(@RequestBody Entity entity){		
		crudRepository.save(entity);
		return  ResponseEntity.ok(entity);
	}	
	
	/**
	 * Appliquer le patch {@code jsonPatchContent} sur {@code originBean}. 
	 * @param originBean L'objet (l'entité) origine et l
	 * @param jsonPatchContent L'opération patch demandée, doit respecter les spécifications Patch <a href=https://tools.ietf.org/html/rfc6902>RFC6902</a> 
	 * @param beanClassType Le type du bean (l'entité)
	 * @return L'objet bean après le patch.
	 * @throws RuntimeException
	 */
	private Entity applyPatch(Entity oldEntity, String jsonPatchContent) {
		
		ObjectMapper objectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
		
		try {
			JsonPatch jsonPatch = objectMapper.readValue(jsonPatchContent, JsonPatch.class);
			JsonNode jsonNodeOriginBean = objectMapper.convertValue(oldEntity, JsonNode.class);
			JsonNode patchedBean = jsonPatch.apply(jsonNodeOriginBean);
			
			// Récupérer le nouveau objet après le patch 
			return objectMapper.treeToValue(patchedBean, targetEntityClass);
			
		} catch (IOException | JsonPatchException ex) {
			throw new RuntimeException(ex);
		}
		
	}
}