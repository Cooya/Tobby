# -*- coding: utf-8 -*-

import os
import sys
import codecs
import time


def check_files_diff(reference_dir, new_dir, filename):
	fullpath_ref = os.path.join(reference_dir, filename)
	fullpath_new = os.path.join(new_dir, filename)
	
	file_ref = codecs.open(fullpath_ref, encoding="utf-8").read()
	file_new = codecs.open(fullpath_new, encoding="utf-8").read()
	
	if file_ref != file_new:
		print("Modified: " + filename)
		return 1
	return 0


def recursive_file_list(root_dir, current_dir, file_list = None):
	if file_list is None:
		file_list = []
	
	dir_list = os.listdir(current_dir)
	
	for dir_item in dir_list:
		full_path = os.path.join(current_dir, dir_item)
		
		if os.path.isfile(full_path):
			file_list.append(full_path.replace(root_dir, "")[1:])
		else:
			recursive_file_list(root_dir, full_path, file_list)
	
	return file_list


def recursive_files_diff(reference_dir, new_dir):
	time_delta = time.time()
	reference_list = recursive_file_list(reference_dir, reference_dir)
	new_list = recursive_file_list(new_dir, new_dir)
	modified_count = 0
	new_count = 0
	deleted_count = 0
	
	for file in new_list:
		if file in reference_list:
			modified_count += check_files_diff(reference_dir, new_dir, file)
			reference_list.remove(file)
		else:
			print("New: " + file)
			new_count += 1
	
	for file in reference_list:
		print("Deleted: " + file)
		deleted_count += 1
	
	time_delta = time.time() - time_delta
	
	print("\n========== Summary ==========")
	print("Modified: " + str(modified_count))
	print("New: " + str(new_count))
	print("Deleted: " + str(deleted_count))
	print("Elapsed time: " + str(time_delta) + " seconds")
	print("=============================")



def main(argc, argv):
	if argc < 3:
		print("Usage: ./recursive_files_diff.py REFERENCE_DIR NEW_DIR")
		return 1
	
	recursive_files_diff(argv[1], argv[2])
	return 0

if __name__ == "__main__":
	sys.exit(main(len(sys.argv), sys.argv))
