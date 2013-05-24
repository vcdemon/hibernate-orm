/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.metamodel.internal.source.annotations;

import java.util.EnumSet;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.JandexAntTask;

import org.hibernate.cfg.NamingStrategy;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.internal.Binder;
import org.hibernate.metamodel.internal.source.annotations.attribute.MappedAttribute;
import org.hibernate.metamodel.internal.source.annotations.attribute.PluralAssociationAttribute;
import org.hibernate.metamodel.internal.source.annotations.entity.ConfiguredClass;
import org.hibernate.metamodel.internal.source.annotations.util.JPADotNames;
import org.hibernate.metamodel.internal.source.annotations.util.JandexHelper;
import org.hibernate.metamodel.spi.source.IndexedPluralAttributeSource;
import org.hibernate.metamodel.spi.source.MappingException;
import org.hibernate.metamodel.spi.source.PluralAttributeIndexSource;

/**
 * @author Strong Liu <stliu@hibernate.org>
 */
public class IndexedPluralAttributeSourceImpl extends PluralAttributeSourceImpl
		implements IndexedPluralAttributeSource {
	private final PluralAttributeIndexSource indexSource;
	private final static EnumSet<MappedAttribute.Nature> VALID_NATURES = EnumSet.of(
			MappedAttribute.Nature.MANY_TO_MANY,
			MappedAttribute.Nature.ONE_TO_MANY,
			MappedAttribute.Nature.ELEMENT_COLLECTION_BASIC,
			MappedAttribute.Nature.ELEMENT_COLLECTION_EMBEDDABLE);

	public IndexedPluralAttributeSourceImpl(
			final PluralAssociationAttribute attribute,
			ConfiguredClass entityClass ) {
		super( attribute, entityClass );
		if ( !VALID_NATURES.contains( attribute.getNature() ) ) {
			throw new MappingException(
					"Indexed column could be only mapped on the MANY side",
					attribute.getContext().getOrigin()
			);
		}
		// TODO: add checks for inconsistent annotations
		if ( attribute.isSequentiallyIndexed() ) {
			final Binder.DefaultNamingStrategy defaultNamingStrategy = new Binder.DefaultNamingStrategy() {
				@Override
				public String defaultName() {
					return attribute.getName() + "_ORDER";
				}
			};
			indexSource = new SequentialPluralAttributeIndexSourceImpl( this, attribute, defaultNamingStrategy );
		}
		else if ( attribute.annotations().containsKey( JPADotNames.MAP_KEY ) ) {
			// basic
			throw new NotYetImplementedException( "@MapKey is not supported yet." );
		}
		else if ( attribute.annotations().containsKey( JPADotNames.MAP_KEY_COLUMN ) ) {
			final Binder.DefaultNamingStrategy defaultNamingStrategy = new Binder.DefaultNamingStrategy() {
				@Override
				public String defaultName() {
					return attribute.getName() + "_KEY";
				}
			};
			indexSource = new BasicPluralAttributeIndexSourceImpl( this, attribute, defaultNamingStrategy );
		}
		else if ( attribute.annotations().containsKey( JPADotNames.MAP_KEY_ENUMERATED ) ) {
			// basic
			throw new NotYetImplementedException( "@MapKeyEnumerated is not supported yet." );
		}
		else if ( attribute.annotations().containsKey( JPADotNames.MAP_KEY_TEMPORAL ) ) {
			// basic
			throw new NotYetImplementedException( "@MapKeyTemporal is not supported yet." );
		}
		else if ( attribute.annotations().containsKey( JPADotNames.MAP_KEY_CLASS ) ) {
			// can be anything
			throw new NotYetImplementedException( "@MapKeyClass is not supported yet." );
		}
		else if ( attribute.annotations().containsKey( JPADotNames.MAP_KEY_JOIN_COLUMN ) ) {
			// association
			throw new NotYetImplementedException( "@MapKeyJoinColumn is not supported yet." );
		}
		else if ( attribute.annotations().containsKey( JPADotNames.MAP_KEY_JOIN_COLUMNS ) ) {
			// association
			throw new NotYetImplementedException( "@MapKeyJoinColumns is not supported yet." );
		}
		else if ( String.class.equals( attribute.getIndexType() ) || attribute.getIndexType().isPrimitive() ) {
			final Binder.DefaultNamingStrategy defaultNamingStrategy = new Binder.DefaultNamingStrategy() {
				@Override
				public String defaultName() {
					return attribute.getName() + "_KEY";
				}
			};
			indexSource = new BasicPluralAttributeIndexSourceImpl( this, attribute, defaultNamingStrategy );
		}
		else {
			// either @Embeddable or entity type.

			// composite:
			// index is @Embeddable
			// @MapKeyClass is not basic, not entity type

			// association:
			// MapKeyJoinColumn, MapKeyJoinColumns are present
			// If the primary key of the referenced entity is not a simple primary key, must have MapKeyJoinColumns.
			//indexSource = new BasicPluralAttributeIndexSourceImpl( this, attribute );
			throw new NotYetImplementedException( "Embeddable and entity keys are not supported yet." );
		}
	}

	@Override
	public PluralAttributeIndexSource getIndexSource() {
		return indexSource;
	}
}
