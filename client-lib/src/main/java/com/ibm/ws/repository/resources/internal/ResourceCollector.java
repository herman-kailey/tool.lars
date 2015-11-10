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
package com.ibm.ws.repository.resources.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.ibm.ws.repository.resources.RepositoryResource;

/**
 * Collects the Resources added to it, optionally discarding duplicates. Once resources have been added to the ResourceCollector, a collection of the added resources can be
 * retrieved with {@link #getResourceCollection()}.
 * <p>
 * This class uses the {@link RepositoryResourceMatchingData} from each resource to determine whether two given resources are duplicates or not.
 * <p>
 * This class is designed to be used to discard duplicate resources when retrieving resources from multiple repositories.
 */
public class ResourceCollector<T extends RepositoryResource> {

    final private HashSet<RepositoryResourceMatchingData> _dupesChecker;
    final private ArrayList<T> _backingCollection = new ArrayList<T>();

    public ResourceCollector(DuplicatePolicy duplicatePolicy) {
        if (duplicatePolicy == DuplicatePolicy.FORBID_DUPLICATES) {
            _dupesChecker = new HashSet<RepositoryResourceMatchingData>();
        } else {
            _dupesChecker = null;
        }
    }

    /**
     * Adds a resource to the collector.
     * <p>
     * If {@link DuplicatePolicy#FORBID_DUPLICATES} was passed to the constructor, the {@link RepositoryResourceMatchingData} of the resource will be compared to each resource in
     * the collection. If a match is found, the new resource will not be added to the collector.
     * 
     * @param res the resource to add to the collector
     * @return true if the resource was successfully added to the collector, otherwise false
     */
    public boolean add(T res) {
        if (_dupesChecker != null) {
            RepositoryResourceMatchingData matchingData = ((RepositoryResourceImpl) res).createMatchingData();
            if (!_dupesChecker.contains(matchingData)) {
                _dupesChecker.add(matchingData);
                _backingCollection.add(res);
                return true;
            } else {
                return false;
            }
        } else {
            _backingCollection.add(res);
            return true;
        }
    }

    /**
     * Adds many resources to the collector.
     * <p>
     * This method is equivalent to calling {@link #add(RepositoryResource)} on each resource returned by iterable.
     * 
     * @param iterable the resources to add
     * @return true if any resources were successfully added to the collector, otherwise false
     */
    public boolean addAll(Iterable<? extends T> iterable) {
        boolean ret = false;
        for (T item : iterable) {
            ret |= add(item);
        }
        return ret;
    }

    /**
     * Returns the resources which have been successfully added to the collector.
     * <p>
     * No further resources should be added to the collector after calling this method.
     * 
     * @return a collection of resources which have been successfully added to the collector
     */
    public Collection<T> getResourceCollection() {
        return _backingCollection;
    }

    public enum DuplicatePolicy {
        ALLOW_DUPLICATES,
        FORBID_DUPLICATES
    }

}