/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ibm.ws.repository.strategies.writeable;

import java.util.List;

import com.ibm.ws.repository.exceptions.RepositoryBackendException;
import com.ibm.ws.repository.exceptions.RepositoryBadDataException;
import com.ibm.ws.repository.exceptions.RepositoryResourceException;
import com.ibm.ws.repository.exceptions.RepositoryResourceNoConnectionException;
import com.ibm.ws.repository.exceptions.RepositoryResourceValidationException;
import com.ibm.ws.repository.resources.internal.RepositoryResourceImpl;

/**
 *
 */
public interface UploadStrategy {

    public static final UploadStrategy DEFAULT_STRATEGY = new AddThenDeleteStrategy();

    /**
     * Upload the resource using an implementation of this interface
     *
     * @param resource
     * @param matchingResource
     * @throws RepositoryBackendException If there was a problem with the backend
     * @throws RepositoryResourceException If there was a problem with the resource
     */
    public void uploadAsset(RepositoryResourceImpl resource, List<RepositoryResourceImpl> matchingResources) throws RepositoryBackendException, RepositoryResourceException;

    /**
     *
     * @param resource The resource we want to find matches of in the repository
     * @return A list of resources that match the specified resource
     * @throws RepositoryBackendException If there was a problem with tbe backend
     * @throws RepositoryBadDataException If while checking for matching assets we find one with bad version data
     * @throws RepositoryResourceNoConnectionException If no connection has been specified
     * @throws RepositoryResourceValidationException If the resource fails a validation check
     */
    public List<RepositoryResourceImpl> findMatchingResources(RepositoryResourceImpl resource) throws RepositoryResourceValidationException, RepositoryBackendException, RepositoryBadDataException, RepositoryResourceNoConnectionException;

    /**
     * Whether to check the editions on upload. The base strategy sets this to true which should be used, however in certain scenarios this
     * can be overridden.
     *
     * @return
     */
    public boolean performEditionChecking();
}
