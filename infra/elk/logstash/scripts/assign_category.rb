require 'json'

def filter(event)
  # 최초 한 번만 로딩
  if !$category_map
    path = "/usr/share/logstash/config/reverse_mapping.json"
    $category_map = JSON.parse(File.read(path))
  end

  desc = event.get("description")
  if desc && $category_map[desc]
    main_cat, sub_cat = $category_map[desc]
    event.set("category", main_cat)
    event.set("subcategory", sub_cat)
  else
    event.set("category", "otherGoodsServices")
    event.set("subcategory", "miscellaneous")
  end

  return [event]
end