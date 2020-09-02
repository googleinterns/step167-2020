import React from "react";
import PropTypes from "prop-types";
import Select from "react-select";

const SortTypeSelect = props => {
  const options = [
    { value: "NEW", label: "New" },
    { value: "TOP", label: "Top of last week" },
  ];

  return (
    <Select
      id="sort-select"
      name="sort-select"
      isMulti
      options={options}
      onChange={selected => {
        if (selected === null) {
          props.setSortType("NONE");
          return;
        }
        props.setSortType(selected.value);
      }}
    />
  );
};

SortTypeSelect.propTypes = {
  sortType: PropTypes.string,
  setSortType: PropTypes.func,
};

export default SortTypeSelect;
