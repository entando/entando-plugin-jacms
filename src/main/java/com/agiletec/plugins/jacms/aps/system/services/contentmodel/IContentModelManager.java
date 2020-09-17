/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.agiletec.plugins.jacms.aps.system.services.contentmodel;

import org.entando.entando.ent.exception.EntException;
import com.agiletec.aps.system.services.page.IPage;
import com.agiletec.plugins.jacms.aps.system.services.content.model.SmallContentType;
import com.agiletec.plugins.jacms.aps.system.services.contentmodel.model.ContentModelReference;
import java.util.List;
import java.util.Map;

/**
 * Interfaccia base per i Manager dei modelli di contenuto.
 *
 * @author E.Santoboni
 */
public interface IContentModelManager {

    /**
     * Aggiunge un modello di contenuto nel sistema.
     *
     * @param model Il modello da aggiungere.
     * @throws EntException In caso di errori in accesso al db.
     */
    public void addContentModel(ContentModel model) throws EntException;

    /**
     * Rimuove un modello di contenuto dal sistema.
     *
     * @param model Il modello di contenuto da rimuovere.
     * @throws EntException In caso di errori in accesso al db.
     */
    public void removeContentModel(ContentModel model) throws EntException;

    /**
     * Aggiorna un modello di contenuto.
     *
     * @param model Il modello di contenuto da aggiornare.
     * @throws EntException In caso di errori in accesso al db.
     */
    public void updateContentModel(ContentModel model) throws EntException;

    /**
     * Restituisce il modello relativo all'identificativo immesso.
     *
     * @param contentModelId L'identificativo del modello da estrarre.
     * @return Il modello cercato.
     */
    public ContentModel getContentModel(long contentModelId);

    /**
     * Restituisce la lista dei modelli di contenuto presenti nel sistema.
     *
     * @return La lista dei modelli di contenuto presenti nel sistema.
     */
    public List<ContentModel> getContentModels();

    /**
     * Restituisce la lista di modelli compatibili con il tipo di contenuto specificato.
     *
     * @param contentType Il codice del tipo di contenuto.
     * @return La lista di modelli compatibili con il tipo di contenuto specificato.
     */
    public List<ContentModel> getModelsForContentType(String contentType);

    /**
     * Restituisce la mappa delle pagine referenziate dal modello di contenuto specificato. La mappa è indicizzata in
     * base ai codici dei contenuti pubblicati tramite il modello specificato, ed il valore è rappresentato dalla lista
     * di pagine nel quale è pubblicato esplicitamente il contenuto (traite il modello specificato).
     *
     * @param modelId Identificativo del modello di contenuto
     * @return La Mappa delle pagine referenziate.
     */
    @Deprecated
    public Map<String, List<IPage>> getReferencingPages(long modelId);

    public List<ContentModelReference> getContentModelReferences(long modelId, boolean includeDefaultTemplateReferences);

    public SmallContentType getDefaultUtilizer(long modelId);

}
