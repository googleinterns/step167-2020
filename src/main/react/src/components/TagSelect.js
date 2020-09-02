import React, { useState, useEffect } from "react";
import PropTypes from "prop-types";
import Select from "react-select";
import { getTags } from "../requests";

const TagSelect = props => {
  const [allTags, setAllTags] = useState([]);

  let maxTags = 10; // Constraint for Firestore Queries

  if (props.maxTags && props.maxTags < 10) {
    maxTags = props.maxTags;
  }

  useEffect(() => {
    getTags().then(data => setAllTags(data));
  }, []);

  return (
    <Select
      id="tags-select"
      name="tags-select"
      isMulti
      options={
        Object.keys(props.tags).length >= maxTags
          ? []
          : Object.keys(allTags).map(tagId => {
              return { label: allTags[tagId].name, value: tagId };
            })
      }
      onChange={selected => {
        if (selected === null) {
          props.setTags({});
          return;
        }
        let newTags = {};
        selected.forEach(tag => (newTags[tag.value] = tag.label));
        props.setTags(newTags);
      }}
    />
  );
};

TagSelect.propTypes = {
  tags: PropTypes.object,
  setTags: PropTypes.func,
  maxTags: PropTypes.number,
};

export default TagSelect;
